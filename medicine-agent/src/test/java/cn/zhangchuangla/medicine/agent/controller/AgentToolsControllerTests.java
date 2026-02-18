package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentDrugToolsController;
import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentOrderToolsController;
import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentProductToolsController;
import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentUserToolsController;
import cn.zhangchuangla.medicine.agent.controller.client.ClientAgentUserToolsController;
import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.*;
import cn.zhangchuangla.medicine.agent.service.MallOrderService;
import cn.zhangchuangla.medicine.agent.service.MallProductService;
import cn.zhangchuangla.medicine.agent.service.UserService;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.dto.AuthUserDto;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.request.MallProductListQueryRequest;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AgentToolsControllerTests {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private final StubUserService agentUserService = new StubUserService();
    private final StubMallProductService agentProductService = new StubMallProductService();
    private final StubMallOrderService agentOrderService = new StubMallOrderService();

    private final AgentAuthorizationController authorizationController = new AgentAuthorizationController(agentUserService);
    private final AdminAgentUserToolsController adminUserController = new AdminAgentUserToolsController(agentUserService);
    private final ClientAgentUserToolsController clientUserController = new ClientAgentUserToolsController(agentUserService);
    private final AdminAgentProductToolsController adminProductController = new AdminAgentProductToolsController(agentProductService);
    private final AdminAgentDrugToolsController adminDrugController = new AdminAgentDrugToolsController(agentProductService);
    private final AdminAgentOrderToolsController adminOrderController = new AdminAgentOrderToolsController(agentOrderService);

    @BeforeEach
    void setUp() {
        loginAs(1001L, "test-user");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authorizationController_ShouldReturnCurrentAuthUser() {
        AuthUserDto dto = new AuthUserDto();
        dto.setId(1001L);
        dto.setUsername("test-user");
        agentUserService.authUser = dto;

        var result = authorizationController.getCurrentUser();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1001L, result.getData().getId());
        assertEquals(1001L, agentUserService.capturedAuthUserId);
    }

    @Test
    void userControllers_ShouldDelegateToUserService() {
        UserVo userVo = new UserVo();
        userVo.setId(1001L);
        userVo.setNickName("管理员");
        agentUserService.currentUser = userVo;

        var adminResult = adminUserController.getCurrentUser();
        var clientResult = clientUserController.getCurrentUser();

        assertEquals(200, adminResult.getCode());
        assertEquals(200, clientResult.getCode());
        assertEquals("管理员", adminResult.getData().getNickName());
        assertEquals("管理员", clientResult.getData().getNickName());
        assertEquals(1001L, agentUserService.capturedCurrentUserId);
    }

    @Test
    void adminProductList_ShouldMapCoverImageToListVo() {
        MallProductDetailDto dto = new MallProductDetailDto();
        dto.setId(200L);
        dto.setName("维生素C");
        dto.setImages(List.of("cover.jpg", "detail.jpg"));
        dto.setStatus(1);
        dto.setDeliveryType(2);

        Page<MallProductDetailDto> page = new Page<>(2, 20, 1);
        page.setRecords(List.of(dto));
        agentProductService.productPage = page;

        var result = adminProductController.searchProducts(null);

        assertEquals(200, result.getCode());
        TableDataResult table = result.getData();
        assertNotNull(table);
        assertEquals(1L, table.getTotal());
        assertEquals(2L, table.getPageNum());
        assertEquals(20L, table.getPageSize());
        assertEquals(1, table.getRows().size());

        AdminAgentProductListVo row = (AdminAgentProductListVo) table.getRows().getFirst();
        assertEquals(200L, row.getId());
        assertEquals("cover.jpg", row.getCoverImage());

        JsonNode rowNode = serializeToNode(row);
        assertEquals("上架", rowNode.get("status").get("description").asText());
        assertEquals(1, rowNode.get("status").get("value").asInt());
        assertEquals("快递配送", rowNode.get("deliveryType").get("description").asText());
        assertEquals(2, rowNode.get("deliveryType").get("value").asInt());
        assertNotNull(agentProductService.capturedListRequest);
    }

    @Test
    void adminProductAndDrugDetail_ShouldDelegateToService() {
        AdminAgentProductDetailVo productDetail = new AdminAgentProductDetailVo();
        productDetail.setId(301L);
        productDetail.setName("感冒灵");
        productDetail.setStatus(1);
        productDetail.setDeliveryType(1);

        AdminAgentDrugDetailVo drugDetail = new AdminAgentDrugDetailVo();
        drugDetail.setProductId(301L);
        drugDetail.setProductName("感冒灵");

        agentProductService.productDetails = List.of(productDetail);
        agentProductService.drugDetails = List.of(drugDetail);

        var productResult = adminProductController.getProductDetail(List.of(301L, 302L));
        var drugResult = adminDrugController.getDrugDetail(List.of(301L));

        assertEquals(200, productResult.getCode());
        assertEquals(200, drugResult.getCode());
        assertEquals("感冒灵", productResult.getData().getFirst().getName());
        JsonNode productNode = serializeToNode(productResult.getData().getFirst());
        assertEquals("上架", productNode.get("status").get("description").asText());
        assertEquals(1, productNode.get("status").get("value").asInt());
        assertEquals("自提", productNode.get("deliveryType").get("description").asText());
        assertEquals(1, productNode.get("deliveryType").get("value").asInt());
        assertEquals(301L, drugResult.getData().getFirst().getProductId());
        assertEquals(List.of(301L, 302L), agentProductService.capturedProductDetailIds);
        assertEquals(List.of(301L), agentProductService.capturedDrugDetailIds);
    }

    @Test
    void adminOrderList_ShouldMapProductInfo() {
        OrderWithProductDto dto = new OrderWithProductDto();
        dto.setId(401L);
        dto.setOrderNo("O401");
        dto.setTotalAmount(new BigDecimal("88.00"));
        dto.setPayType("ALIPAY");
        dto.setOrderStatus("PENDING_PAYMENT");
        dto.setProductId(9001L);
        dto.setProductName("阿莫西林");
        dto.setProductImage("p.jpg");
        dto.setProductPrice(new BigDecimal("44.00"));
        dto.setProductCategory("抗生素");
        dto.setProductQuantity(2);

        Page<OrderWithProductDto> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dto));
        agentOrderService.orderPage = page;

        var result = adminOrderController.getOrderList(null);

        assertEquals(200, result.getCode());
        TableDataResult table = result.getData();
        assertNotNull(table);
        assertEquals(1, table.getRows().size());

        AdminMallOrderListVo row = (AdminMallOrderListVo) table.getRows().getFirst();
        assertEquals("O401", row.getOrderNo());
        assertNotNull(row.getProductInfo());
        assertEquals(9001L, row.getProductInfo().getProductId());
        assertEquals(2, row.getProductInfo().getQuantity());

        JsonNode rowNode = serializeToNode(row);
        assertEquals("使用支付宝进行支付", rowNode.get("payType").get("description").asText());
        assertEquals("ALIPAY", rowNode.get("payType").get("value").asText());
        assertEquals("待支付", rowNode.get("orderStatus").get("description").asText());
        assertEquals("PENDING_PAYMENT", rowNode.get("orderStatus").get("value").asText());
        assertNotNull(agentOrderService.capturedListRequest);
    }

    @Test
    void adminOrderDetail_ShouldDelegateToService() {
        AdminOrderDetailVo detailVo = new AdminOrderDetailVo();
        AdminOrderDetailVo.OrderInfo orderInfo = new AdminOrderDetailVo.OrderInfo();
        orderInfo.setOrderNo("OD-1");
        orderInfo.setPayType("WECHAT_PAY");
        orderInfo.setOrderStatus("PENDING_SHIPMENT");
        detailVo.setOrderInfo(orderInfo);
        agentOrderService.orderDetails = List.of(detailVo);

        var result = adminOrderController.getOrderDetail(List.of(501L, 502L));

        assertEquals(200, result.getCode());
        assertEquals("OD-1", result.getData().getFirst().getOrderInfo().getOrderNo());
        JsonNode orderInfoNode = serializeToNode(result.getData().getFirst().getOrderInfo());
        assertEquals("使用微信支付进行支付", orderInfoNode.get("payType").get("description").asText());
        assertEquals("WECHAT_PAY", orderInfoNode.get("payType").get("value").asText());
        assertEquals("待发货", orderInfoNode.get("orderStatus").get("description").asText());
        assertEquals("PENDING_SHIPMENT", orderInfoNode.get("orderStatus").get("value").asText());
        assertEquals(List.of(501L, 502L), agentOrderService.capturedOrderDetailIds);
    }

    private JsonNode serializeToNode(Object value) {
        try {
            return objectMapper.readTree(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("serialize value failed", ex);
        }
    }

    private void loginAs(Long userId, String username) {
        AuthUser authUser = AuthUser.builder()
                .id(userId)
                .username(username)
                .roles(Set.of("ROLE_admin"))
                .build();
        SysUserDetails details = new SysUserDetails(authUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );
    }

    private static class StubUserService implements UserService {

        private UserVo currentUser;
        private AuthUserDto authUser;
        private Long capturedCurrentUserId;
        private Long capturedAuthUserId;

        @Override
        public UserVo getCurrentUser(Long userId) {
            this.capturedCurrentUserId = userId;
            return currentUser;
        }

        @Override
        public AuthUserDto getUser(Long userId) {
            this.capturedAuthUserId = userId;
            return authUser;
        }

        @Override
        public cn.zhangchuangla.medicine.model.entity.User getUserByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getUserRolesByUserId(Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getUserPermissionCodesByUserId(Long userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StubMallProductService implements MallProductService {

        private Page<MallProductDetailDto> productPage = new Page<>();
        private List<AdminAgentProductDetailVo> productDetails = List.of();
        private List<AdminAgentDrugDetailVo> drugDetails = List.of();

        private MallProductListQueryRequest capturedListRequest;
        private List<Long> capturedProductDetailIds;
        private List<Long> capturedDrugDetailIds;

        @Override
        public Page<MallProductDetailDto> listProducts(MallProductListQueryRequest request) {
            this.capturedListRequest = request;
            return productPage;
        }

        @Override
        public List<AdminAgentProductDetailVo> getProductDetail(List<Long> productIds) {
            this.capturedProductDetailIds = productIds;
            return productDetails;
        }

        @Override
        public List<AdminAgentDrugDetailVo> getDrugDetail(List<Long> productIds) {
            this.capturedDrugDetailIds = productIds;
            return drugDetails;
        }
    }

    private static class StubMallOrderService implements MallOrderService {

        private Page<OrderWithProductDto> orderPage = new Page<>();
        private List<AdminOrderDetailVo> orderDetails = List.of();

        private AdminMallOrderListRequest capturedListRequest;
        private List<Long> capturedOrderDetailIds;

        @Override
        public Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request) {
            this.capturedListRequest = request;
            return orderPage;
        }

        @Override
        public List<AdminOrderDetailVo> getOrderDetail(List<Long> orderIds) {
            this.capturedOrderDetailIds = orderIds;
            return orderDetails;
        }
    }
}
