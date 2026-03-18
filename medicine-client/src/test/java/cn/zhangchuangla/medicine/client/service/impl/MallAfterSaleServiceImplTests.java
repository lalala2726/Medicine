package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallAfterSaleMapper;
import cn.zhangchuangla.medicine.client.model.request.AfterSaleApplyRequest;
import cn.zhangchuangla.medicine.client.model.request.AfterSaleEligibilityRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        doReturn(66L).when(service).getUserId();

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
        when(mallOrderItemService.getById(9L)).thenReturn(createOrderItem(9L, new BigDecimal("29.90"), BigDecimal.ZERO));
        when(mallAfterSaleTimelineService.getTimelineList(1L)).thenReturn(List.of(createTimeline()));

        var result = service.getAfterSaleDetail("AS202511130001", 66L);

        assertNotNull(result);
        assertEquals("AS202511130001", result.getAfterSaleNo());
        assertEquals("测试用户", result.getUserNickname());
        assertEquals("待审核", result.getAfterSaleStatusName());
        assertNotNull(result.getProductInfo());
        assertEquals("999感冒灵颗粒-9", result.getProductInfo().getProductName());
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
        mockOrderQuery(createCompletedOrder("O202511130001", new BigDecimal("59.80"), BigDecimal.ZERO, daysAgo(10)));
        mockOrderItemsQuery(List.of(
                createOrderItem(9L, new BigDecimal("29.90"), new BigDecimal("10.00")),
                createOrderItem(10L, new BigDecimal("29.90"), BigDecimal.ZERO)
        ));
        LambdaQueryChainWrapper<MallAfterSale> activeQuery1 = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        LambdaQueryChainWrapper<MallAfterSale> activeQuery2 = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(activeQuery1.count()).thenReturn(0L);
        when(activeQuery2.count()).thenReturn(0L);
        mockAfterSaleQuerySequence(activeQuery1, activeQuery2);

        ClientAgentAfterSaleEligibilityRequest request = new ClientAgentAfterSaleEligibilityRequest();
        request.setOrderNo("O202511130001");
        request.setOrderItemId(9L);

        var result = service.checkAfterSaleEligibility(request, 66L);

        assertTrue(result.getEligible());
        assertEquals("ITEM", result.getScope());
        assertEquals("ELIGIBLE", result.getReasonCode());
        assertEquals(new BigDecimal("19.90"), result.getRefundableAmount());
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkAfterSaleEligibility_WhenCompletedOrderExpired_ShouldReturnExpiredReason() {
        mockOrderQuery(createCompletedOrder("O202511130001", new BigDecimal("29.90"), BigDecimal.ZERO, daysAgo(95)));
        mockOrderItemsQuery(List.of(createOrderItem(9L, new BigDecimal("29.90"), BigDecimal.ZERO)));

        ClientAgentAfterSaleEligibilityRequest request = new ClientAgentAfterSaleEligibilityRequest();
        request.setOrderNo("O202511130001");

        var result = service.checkAfterSaleEligibility(request, 66L);

        assertFalse(result.getEligible());
        assertEquals("AFTER_SALE_EXPIRED", result.getReasonCode());
        assertEquals("订单确认收货已超过3个月，无法申请售后", result.getReasonMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAfterSaleEligibility_ShouldReturnItemsAndAmounts() {
        doReturn(66L).when(service).getUserId();
        mockOrderQuery(createCompletedOrder("O202511130001", new BigDecimal("59.80"), BigDecimal.ZERO, daysAgo(7)));
        mockOrderItemsQuery(List.of(
                createOrderItem(9L, new BigDecimal("19.90"), BigDecimal.ZERO),
                createOrderItem(10L, new BigDecimal("39.90"), BigDecimal.ZERO)
        ));
        LambdaQueryChainWrapper<MallAfterSale> activeQuery1 = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        LambdaQueryChainWrapper<MallAfterSale> activeQuery2 = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(activeQuery1.count()).thenReturn(0L);
        when(activeQuery2.count()).thenReturn(0L);
        mockAfterSaleQuerySequence(activeQuery1, activeQuery2);

        AfterSaleEligibilityRequest request = new AfterSaleEligibilityRequest();
        request.setOrderNo("O202511130001");
        request.setScope(AfterSaleScopeEnum.ORDER);

        var result = service.getAfterSaleEligibility(request);

        assertTrue(result.getEligible());
        assertEquals("ORDER", result.getRequestedScope());
        assertEquals("ORDER", result.getResolvedScope());
        assertEquals(new BigDecimal("59.80"), result.getTotalRefundableAmount());
        assertEquals(new BigDecimal("59.80"), result.getSelectedRefundableAmount());
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("19.90"), result.getItems().getFirst().getRefundableAmount());
    }

    @SuppressWarnings("unchecked")
    @Test
    void applyAfterSale_ShouldAutoConvertSingleItemToOrderScopeAndIgnoreClientRefundAmount() {
        doReturn(66L).when(service).getUserId();
        doReturn("tester").when(service).getUsername();
        mockOrderQuery(createCompletedOrder("O202511130001", new BigDecimal("29.90"), BigDecimal.ZERO, daysAgo(5)));
        mockOrderItemsQuery(List.of(createOrderItem(9L, new BigDecimal("29.90"), new BigDecimal("10.00"))));
        LambdaQueryChainWrapper<MallAfterSale> activeQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        LambdaQueryChainWrapper<MallAfterSale> existingQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(activeQuery.count()).thenReturn(0L);
        when(existingQuery.one()).thenReturn(null);
        mockAfterSaleQuerySequence(activeQuery, existingQuery);
        doAnswer(invocation -> {
            MallAfterSale afterSale = invocation.getArgument(0);
            afterSale.setId(1L);
            return true;
        }).when(service).save(any(MallAfterSale.class));
        when(mallOrderItemService.updateById(any(MallOrderItem.class))).thenReturn(true);
        when(mallOrderService.updateById(any(MallOrder.class))).thenReturn(true);

        AfterSaleApplyRequest request = new AfterSaleApplyRequest();
        request.setOrderNo("O202511130001");
        request.setScope(AfterSaleScopeEnum.ITEM);
        request.setOrderItemId(9L);
        request.setAfterSaleType(AfterSaleTypeEnum.REFUND_ONLY);
        request.setRefundAmount(new BigDecimal("1.00"));
        request.setApplyReason(AfterSaleReasonEnum.DAMAGED);
        request.setApplyDescription("包装破损");

        var result = service.applyAfterSale(request);

        ArgumentCaptor<MallAfterSale> afterSaleCaptor = ArgumentCaptor.forClass(MallAfterSale.class);
        verify(service).save(afterSaleCaptor.capture());
        MallAfterSale savedAfterSale = afterSaleCaptor.getValue();

        assertEquals("ITEM", result.getRequestedScope());
        assertEquals("ORDER", result.getResolvedScope());
        assertEquals(1, result.getAfterSaleNos().size());
        assertEquals(List.of(9L), result.getOrderItemIds());
        assertEquals(savedAfterSale.getAfterSaleNo(), result.getAfterSaleNos().getFirst());
        assertEquals(new BigDecimal("19.90"), savedAfterSale.getRefundAmount());
        assertEquals(ReceiveStatusEnum.RECEIVED.getStatus(), savedAfterSale.getReceiveStatus());
    }

    @Test
    void applyAfterSale_ShouldRejectUnsupportedAfterSaleTypeForOrderScope() {
        AfterSaleApplyRequest request = new AfterSaleApplyRequest();
        request.setOrderNo("O202511130001");
        request.setScope(AfterSaleScopeEnum.ORDER);
        request.setAfterSaleType(AfterSaleTypeEnum.EXCHANGE);
        request.setApplyReason(AfterSaleReasonEnum.DAMAGED);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.applyAfterSale(request));

        assertEquals(ResponseCode.OPERATION_ERROR.getCode(), exception.getCode());
        assertEquals("整单申请仅支持仅退款", exception.getMessage());
    }

    private void mockOrderQuery(MallOrder order) {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<MallOrder> orderQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(mallOrderService.lambdaQuery()).thenReturn(orderQuery);
        when(orderQuery.one()).thenReturn(order);
    }

    private void mockOrderItemsQuery(List<MallOrderItem> orderItems) {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<MallOrderItem> orderItemQuery = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        when(mallOrderItemService.lambdaQuery()).thenReturn(orderItemQuery);
        when(orderItemQuery.list()).thenReturn(orderItems);
    }

    private void mockAfterSaleQuerySequence(LambdaQueryChainWrapper<MallAfterSale> first,
                                            LambdaQueryChainWrapper<MallAfterSale> second) {
        doReturn(first, second).when(service).lambdaQuery();
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

    private MallOrder createCompletedOrder(String orderNo, BigDecimal payAmount, BigDecimal refundPrice, Date receiveTime) {
        MallOrder order = new MallOrder();
        order.setId(2L);
        order.setOrderNo(orderNo);
        order.setUserId(66L);
        order.setPaid(1);
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getType());
        order.setPayAmount(payAmount);
        order.setRefundPrice(refundPrice);
        order.setReceiveTime(receiveTime);
        order.setFinishTime(receiveTime);
        return order;
    }

    private MallOrderItem createOrderItem(Long itemId, BigDecimal totalPrice, BigDecimal refundedAmount) {
        MallOrderItem orderItem = new MallOrderItem();
        orderItem.setId(itemId);
        orderItem.setOrderId(2L);
        orderItem.setProductId(10L + itemId);
        orderItem.setProductName("999感冒灵颗粒-" + itemId);
        orderItem.setImageUrl("https://example.com/product.jpg");
        orderItem.setPrice(totalPrice);
        orderItem.setQuantity(1);
        orderItem.setTotalPrice(totalPrice);
        orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.NONE.getStatus());
        orderItem.setRefundedAmount(refundedAmount);
        return orderItem;
    }

    private Date daysAgo(int days) {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
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
