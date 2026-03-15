package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallAfterSaleMapper;
import cn.zhangchuangla.medicine.client.service.*;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.MallAfterSale;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.model.request.ClientAgentAfterSaleEligibilityRequest;
import cn.zhangchuangla.medicine.model.vo.AfterSaleTimelineVo;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MallAfterSaleServiceImplTests {

    @Mock
    private MallAfterSaleMapper mallAfterSaleMapper;
    @Mock
    private MallAfterSaleTimelineService mallAfterSaleTimelineService;
    @Mock
    private MallOrderItemService mallOrderItemService;
    @Mock
    private MallOrderTimelineService mallOrderTimelineService;
    @Mock
    private MallOrderService mallOrderService;
    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private MallAfterSaleServiceImpl service;

    @SuppressWarnings("unchecked")
    @Test
    void getAfterSaleDetail_WhenAfterSaleDoesNotBelongToUser_ShouldThrowNotFound() {
        LambdaQueryChainWrapper<MallAfterSale> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(query).when(service).lambdaQuery();
        when(query.one()).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.getAfterSaleDetail("AS202511130001", 66L));

        assertEquals(ResponseCode.RESULT_IS_NULL.getCode(), exception.getCode());
        assertEquals("售后申请不存在", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAfterSaleDetail_ShouldAssembleProductInfoAndTimeline() {
        LambdaQueryChainWrapper<MallAfterSale> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(query).when(service).lambdaQuery();
        when(query.one()).thenReturn(createAfterSale());
        when(userService.getById(66L)).thenReturn(createUser());
        when(mallOrderItemService.getById(9L)).thenReturn(createOrderItem());
        when(mallAfterSaleTimelineService.getTimelineList(1L)).thenReturn(List.of(createTimeline()));

        var result = service.getAfterSaleDetail("AS202511130001", 66L);

        assertNotNull(result);
        assertEquals("AS202511130001", result.getAfterSaleNo());
        assertEquals("测试用户", result.getUserNickname());
        assertEquals("待审核", result.getAfterSaleStatusName());
        assertNotNull(result.getProductInfo());
        assertEquals("999感冒灵颗粒", result.getProductInfo().getProductName());
        assertEquals(1, result.getTimeline().size());
        verify(mallAfterSaleTimelineService).getTimelineList(1L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkAfterSaleEligibility_WhenOrderNotFound_ShouldReturnIneligibleResult() {
        LambdaQueryChainWrapper<MallOrder> orderQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(mallOrderService.lambdaQuery()).thenReturn(orderQuery);
        when(orderQuery.one()).thenReturn(null);

        ClientAgentAfterSaleEligibilityRequest request = new ClientAgentAfterSaleEligibilityRequest();
        request.setOrderNo("O202511130001");

        var result = service.checkAfterSaleEligibility(request, 66L);

        assertFalse(result.getEligible());
        assertEquals("ORDER", result.getScope());
        assertEquals("ORDER_NOT_FOUND", result.getReasonCode());
        assertEquals("订单不存在或无权访问", result.getReasonMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkAfterSaleEligibility_ForOrderItem_ShouldReturnRefundableAmountWhenEligible() {
        LambdaQueryChainWrapper<MallOrder> orderQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(mallOrderService.lambdaQuery()).thenReturn(orderQuery);
        when(orderQuery.one()).thenReturn(createCompletedOrder());

        LambdaQueryChainWrapper<MallAfterSale> activeAfterSaleQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(activeAfterSaleQuery).when(service).lambdaQuery();
        when(activeAfterSaleQuery.count()).thenReturn(0L);
        when(mallOrderItemService.getById(9L)).thenReturn(createEligibleOrderItem());

        ClientAgentAfterSaleEligibilityRequest request = new ClientAgentAfterSaleEligibilityRequest();
        request.setOrderNo("O202511130001");
        request.setOrderItemId(9L);

        var result = service.checkAfterSaleEligibility(request, 66L);

        assertTrue(result.getEligible());
        assertEquals("ITEM", result.getScope());
        assertEquals("ELIGIBLE", result.getReasonCode());
        assertEquals(new BigDecimal("19.90"), result.getRefundableAmount());
    }

    private MallAfterSale createAfterSale() {
        MallAfterSale afterSale = new MallAfterSale();
        afterSale.setId(1L);
        afterSale.setAfterSaleNo("AS202511130001");
        afterSale.setOrderId(2L);
        afterSale.setOrderNo("O202511130001");
        afterSale.setOrderItemId(9L);
        afterSale.setUserId(66L);
        afterSale.setAfterSaleType(AfterSaleTypeEnum.REFUND_ONLY.getType());
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.PENDING.getStatus());
        afterSale.setRefundAmount(new BigDecimal("29.90"));
        afterSale.setApplyReason(AfterSaleReasonEnum.DAMAGED.getReason());
        afterSale.setReceiveStatus(ReceiveStatusEnum.RECEIVED.getStatus());
        afterSale.setApplyTime(new Date());
        return afterSale;
    }

    private User createUser() {
        User user = new User();
        user.setId(66L);
        user.setNickname("测试用户");
        return user;
    }

    private MallOrderItem createOrderItem() {
        MallOrderItem orderItem = new MallOrderItem();
        orderItem.setId(9L);
        orderItem.setProductId(10L);
        orderItem.setProductName("999感冒灵颗粒");
        orderItem.setImageUrl("https://example.com/product.jpg");
        orderItem.setPrice(new BigDecimal("29.90"));
        orderItem.setQuantity(1);
        orderItem.setTotalPrice(new BigDecimal("29.90"));
        return orderItem;
    }

    private MallOrder createCompletedOrder() {
        MallOrder order = new MallOrder();
        order.setId(2L);
        order.setOrderNo("O202511130001");
        order.setUserId(66L);
        order.setPaid(1);
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getType());
        order.setPayAmount(new BigDecimal("29.90"));
        order.setRefundPrice(BigDecimal.ZERO);
        return order;
    }

    private MallOrderItem createEligibleOrderItem() {
        MallOrderItem orderItem = createOrderItem();
        orderItem.setOrderId(2L);
        orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.NONE.getStatus());
        orderItem.setRefundedAmount(new BigDecimal("10.00"));
        return orderItem;
    }

    private AfterSaleTimelineVo createTimeline() {
        return AfterSaleTimelineVo.builder()
                .id(1L)
                .eventType("REFUND_APPLY")
                .eventTypeName("退款申请")
                .eventStatus("PENDING")
                .operatorType("USER")
                .operatorTypeName("用户")
                .description("用户申请退款")
                .createTime(new Date())
                .build();
    }
}
