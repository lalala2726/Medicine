package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminMallOrderListVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.OrderDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallOrderService;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理端智能体订单工具控制器单元测试类。
 * <p>
 * 测试目标：验证 {@link AgentOrderController} 的订单查询功能是否正确委托给 Service 层
 * 测试接口：
 * <ul>
 *     <li>GET /agent/admin/order/list - 获取订单列表</li>
 *     <li>GET /agent/admin/order/{orderNos} - 获取订单详情</li>
 * </ul>
 *
 * @author Chuang
 */
class AgentOrderControllerTests {

    private final StubMallOrderService orderService = new StubMallOrderService();
    private final AgentOrderController controller = new AgentOrderController(orderService);

    /**
     * 测试订单列表查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 listOrders 方法，
     * 并将返回结果转换为正确的响应格式。
     * 测试接口：GET /agent/admin/order/list
     * 预期结果：返回状态码 200，数据包含正确的分页信息
     */
    @Test
    void getOrderList_ShouldDelegateToService() {
        AdminMallOrderListRequest request = new AdminMallOrderListRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        orderService.orderPage = createSampleOrderPage();

        var result = controller.getOrderList(request);

        assertEquals(200, result.getCode());
        assertTrue(orderService.listOrdersInvoked);
        assertEquals(request, orderService.capturedRequest);
    }

    /**
     * 测试空请求参数时使用默认值。
     * <p>
     * 测试目的：验证当传入 null 请求参数时，Controller 能够正确处理，
     * 使用默认的空请求对象进行查询。
     * 测试接口：GET /agent/admin/order/list
     * 预期结果：返回状态码 200，Service 接收到非 null 的请求对象
     */
    @Test
    void getOrderList_WithNullRequest_ShouldUseDefault() {
        orderService.orderPage = createSampleOrderPage();

        var result = controller.getOrderList(null);

        assertEquals(200, result.getCode());
        assertNotNull(orderService.capturedRequest);
    }

    /**
     * 测试订单列表 VO 构建逻辑。
     * <p>
     * 测试目的：验证 Controller 能够正确将 OrderWithProductDto 转换为 AdminMallOrderListVo，
     * 包括订单基本信息和关联的商品信息。
     * 测试接口：GET /agent/admin/order/list
     * 预期结果：返回的列表包含正确的订单和商品信息
     */
    @Test
    void getOrderList_ShouldBuildCorrectVo() {
        AdminMallOrderListRequest request = new AdminMallOrderListRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        orderService.orderPage = createOrderPageWithProduct();

        var result = controller.getOrderList(request);

        assertEquals(200, result.getCode());
        var data = result.getData();
        assertNotNull(data);
        assertNotNull(data.getRows());
        assertEquals(1, data.getRows().size());

        @SuppressWarnings("unchecked")
        List<AdminMallOrderListVo> rows = (List<AdminMallOrderListVo>) data.getRows();
        AdminMallOrderListVo vo = rows.getFirst();
        assertEquals("O202510312122", vo.getOrderNo());
        assertEquals(new BigDecimal("100.00"), vo.getTotalAmount());
        assertNotNull(vo.getProductInfo());
        assertEquals("维生素C片", vo.getProductInfo().getProductName());
    }

    /**
     * 测试订单详情查询是否正确委托给 Service。
     * <p>
     * 测试目的：验证 Controller 正确调用 Service 层的 getOrderDetail 方法，
     * 并将订单编号列表传递给 Service。
     * 测试接口：GET /agent/admin/order/{orderNos}
     * 预期结果：返回状态码 200，包含订单详情列表
     */
    @Test
    void getOrderDetail_ShouldDelegateToService() {
        List<String> orderNos = List.of("O202510312122", "O202510312123");
        orderService.orderDetails = createSampleOrderDetails();

        var result = controller.getOrderDetail(orderNos);

        assertEquals(200, result.getCode());
        assertTrue(orderService.getOrderDetailInvoked);
        assertEquals(orderNos, orderService.capturedOrderNos);
        assertEquals(2, result.getData().size());
    }

    // ==================== Helper Methods ====================

    private Page<OrderWithProductDto> createSampleOrderPage() {
        Page<OrderWithProductDto> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(createSampleOrderWithProduct()));
        return page;
    }

    private Page<OrderWithProductDto> createOrderPageWithProduct() {
        Page<OrderWithProductDto> page = new Page<>(1, 10);
        page.setTotal(1);

        OrderWithProductDto dto = new OrderWithProductDto();
        dto.setId(1L);
        dto.setOrderNo("O202510312122");
        dto.setTotalAmount(new BigDecimal("100.00"));
        dto.setPayType("ALIPAY");
        dto.setOrderStatus("PAID");
        dto.setPayTime(new Date());
        dto.setCreateTime(new Date());
        dto.setProductId(1L);
        dto.setProductName("维生素C片");
        dto.setProductImage("https://example.com/image.jpg");
        dto.setProductPrice(new BigDecimal("50.00"));
        dto.setProductCategory("保健品");
        dto.setProductQuantity(2);

        page.setRecords(List.of(dto));
        return page;
    }

    private OrderWithProductDto createSampleOrderWithProduct() {
        OrderWithProductDto dto = new OrderWithProductDto();
        dto.setId(1L);
        dto.setOrderNo("O202510312122");
        dto.setTotalAmount(new BigDecimal("100.00"));
        dto.setPayType("ALIPAY");
        dto.setOrderStatus("PAID");
        dto.setCreateTime(new Date());
        return dto;
    }

    private List<OrderDetailVo> createSampleOrderDetails() {
        OrderDetailVo detail1 = new OrderDetailVo();
        OrderDetailVo.OrderInfo orderInfo1 = new OrderDetailVo.OrderInfo();
        orderInfo1.setOrderNo("O202510312122");
        orderInfo1.setOrderStatus("PAID");
        detail1.setOrderInfo(orderInfo1);

        OrderDetailVo detail2 = new OrderDetailVo();
        OrderDetailVo.OrderInfo orderInfo2 = new OrderDetailVo.OrderInfo();
        orderInfo2.setOrderNo("O202510312123");
        orderInfo2.setOrderStatus("PENDING_PAYMENT");
        detail2.setOrderInfo(orderInfo2);

        return List.of(detail1, detail2);
    }

    // ==================== Stub Service ====================

    private static class StubMallOrderService implements MallOrderService {

        private Page<OrderWithProductDto> orderPage = new Page<>();
        private List<OrderDetailVo> orderDetails = List.of();

        private boolean listOrdersInvoked;
        private boolean getOrderDetailInvoked;

        private AdminMallOrderListRequest capturedRequest;
        private List<String> capturedOrderNos;

        @Override
        public Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request) {
            this.listOrdersInvoked = true;
            this.capturedRequest = request;
            return orderPage;
        }

        @Override
        public List<OrderDetailVo> getOrderDetail(List<String> orderNos) {
            this.getOrderDetailInvoked = true;
            this.capturedOrderNos = orderNos;
            return orderDetails;
        }
    }
}
