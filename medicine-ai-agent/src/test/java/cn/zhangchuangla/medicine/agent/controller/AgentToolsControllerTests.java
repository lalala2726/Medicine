package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentDrugToolsController;
import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentOrderToolsController;
import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentProductToolsController;
import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentUserToolsController;
import cn.zhangchuangla.medicine.agent.controller.client.ClientAgentUserToolsController;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentDrugDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminAgentProductDetailVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminMallOrderListVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.spi.*;
import cn.zhangchuangla.medicine.agent.spi.test.TestAgentSpiData;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.entity.AuthUser;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.model.dto.MallProductDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.vo.UserVo;
import cn.zhangchuangla.medicine.model.vo.mall.MallProductListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AgentToolsControllerTests {

    private final AdminAgentUserToolsController adminUserController = new AdminAgentUserToolsController();
    private final ClientAgentUserToolsController clientUserController = new ClientAgentUserToolsController();
    private final AdminAgentProductToolsController adminProductController = new AdminAgentProductToolsController();
    private final AdminAgentDrugToolsController adminDrugController = new AdminAgentDrugToolsController();
    private final AdminAgentOrderToolsController adminOrderController = new AdminAgentOrderToolsController();

    @BeforeEach
    void setUp() {
        TestAgentSpiData.reset();
        loginAs(1001L, "test-user");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TestAgentSpiData.reset();
    }

    @Test
    void adminCurrentUser_ShouldUseAdminProviderAndReturnUser() {
        UserVo userVo = new UserVo();
        userVo.setId(1001L);
        userVo.setNickName("管理员");
        TestAgentSpiData.adminUser = userVo;

        var result = adminUserController.getCurrentUser();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1001L, result.getData().getId());
        assertEquals("管理员", result.getData().getNickName());
        assertEquals(1001L, TestAgentSpiData.capturedAdminUserId);
    }

    @Test
    void clientCurrentUser_ShouldUseClientProviderAndReturnUser() {
        UserVo userVo = new UserVo();
        userVo.setId(1001L);
        userVo.setNickName("客户端用户");
        TestAgentSpiData.clientUser = userVo;

        var result = clientUserController.getCurrentUser();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("客户端用户", result.getData().getNickName());
        assertEquals(1001L, TestAgentSpiData.capturedClientUserId);
    }

    @Test
    void adminProductList_ShouldMapCoverImageToMallProductListVo() {
        MallProductDetailDto dto = new MallProductDetailDto();
        dto.setId(200L);
        dto.setName("维生素C");
        dto.setImages(List.of("cover.jpg", "detail.jpg"));

        Page<MallProductDetailDto> page = new Page<>(2, 20, 1);
        page.setRecords(List.of(dto));
        TestAgentSpiData.productPage = page;

        var result = adminProductController.searchProducts(null);

        assertEquals(200, result.getCode());
        assertNotNull(TestAgentSpiData.capturedProductListRequest);

        TableDataResult table = result.getData();
        assertNotNull(table);
        assertEquals(1L, table.getTotal());
        assertEquals(2L, table.getPageNum());
        assertEquals(20L, table.getPageSize());
        assertEquals(1, table.getRows().size());

        MallProductListVo row = (MallProductListVo) table.getRows().getFirst();
        assertEquals(200L, row.getId());
        assertEquals("维生素C", row.getName());
        assertEquals("cover.jpg", row.getCoverImage());
    }

    @Test
    void adminProductAndDrugDetail_ShouldDelegateToProvider() {
        AdminAgentProductDetailVo productDetail = new AdminAgentProductDetailVo();
        productDetail.setId(301L);
        productDetail.setName("感冒灵");
        TestAgentSpiData.productDetails = List.of(productDetail);

        AdminAgentDrugDetailVo drugDetail = new AdminAgentDrugDetailVo();
        drugDetail.setProductId(301L);
        drugDetail.setProductName("感冒灵");
        TestAgentSpiData.drugDetails = List.of(drugDetail);

        var productResult = adminProductController.getProductDetail(List.of(301L, 302L));
        var drugResult = adminDrugController.getDrugDetail(List.of(301L));

        assertEquals(List.of(301L, 302L), TestAgentSpiData.capturedProductDetailIds);
        assertEquals(List.of(301L), TestAgentSpiData.capturedDrugDetailIds);

        assertEquals(200, productResult.getCode());
        assertEquals(1, productResult.getData().size());
        assertEquals("感冒灵", productResult.getData().getFirst().getName());

        assertEquals(200, drugResult.getCode());
        assertEquals(1, drugResult.getData().size());
        assertEquals(301L, drugResult.getData().getFirst().getProductId());
    }

    @Test
    void adminOrderList_ShouldMapProductInfo() {
        OrderWithProductDto dto = new OrderWithProductDto();
        dto.setId(401L);
        dto.setOrderNo("O401");
        dto.setTotalAmount(new BigDecimal("88.00"));
        dto.setProductId(9001L);
        dto.setProductName("阿莫西林");
        dto.setProductImage("p.jpg");
        dto.setProductPrice(new BigDecimal("44.00"));
        dto.setProductCategory("抗生素");
        dto.setProductQuantity(2);

        Page<OrderWithProductDto> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dto));
        TestAgentSpiData.orderPage = page;

        var result = adminOrderController.getOrderList(null);

        assertEquals(200, result.getCode());
        assertNotNull(TestAgentSpiData.capturedOrderListRequest);

        TableDataResult table = result.getData();
        assertEquals(1, table.getRows().size());

        AdminMallOrderListVo row = (AdminMallOrderListVo) table.getRows().getFirst();
        assertEquals("O401", row.getOrderNo());
        assertNotNull(row.getProductInfo());
        assertEquals(9001L, row.getProductInfo().getProductId());
        assertEquals(2, row.getProductInfo().getQuantity());
    }

    @Test
    void adminOrderDetail_ShouldDelegateToProvider() {
        AdminOrderDetailVo detail = new AdminOrderDetailVo();
        AdminOrderDetailVo.OrderInfo orderInfo = new AdminOrderDetailVo.OrderInfo();
        orderInfo.setOrderNo("OD-1");
        detail.setOrderInfo(orderInfo);
        TestAgentSpiData.orderDetails = List.of(detail);

        var result = adminOrderController.getOrderDetail(List.of(501L, 502L));

        assertEquals(List.of(501L, 502L), TestAgentSpiData.capturedOrderDetailIds);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("OD-1", result.getData().getFirst().getOrderInfo().getOrderNo());
    }

    @Test
    void agentSpiLoader_ShouldLoadTestProviders() {
        assertTrue(AgentSpiLoader.hasImplementation(AdminUserDataProvider.class));
        assertTrue(AgentSpiLoader.hasImplementation(AdminProductDataProvider.class));
        assertTrue(AgentSpiLoader.hasImplementation(AdminOrderDataProvider.class));
        assertTrue(AgentSpiLoader.hasImplementation(ClientUserDataProvider.class));
        assertFalse(AgentSpiLoader.hasImplementation(MissingSpi.class));

        assertNotNull(AgentSpiLoader.loadSingle(AdminUserDataProvider.class));
        assertNotNull(AgentSpiLoader.loadSingle(ClientUserDataProvider.class));
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

    private interface MissingSpi {
    }
}
