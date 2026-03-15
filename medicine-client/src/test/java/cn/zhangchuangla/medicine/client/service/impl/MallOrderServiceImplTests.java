package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.client.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.client.service.*;
import cn.zhangchuangla.medicine.client.task.OrderDelayProducer;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderShipping;
import cn.zhangchuangla.medicine.model.entity.MallOrderTimeline;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MallOrderServiceImplTests {

    @Mock
    private MallProductService mallProductService;
    @Mock
    private MallOrderItemService mallOrderItemService;
    @Mock
    private AlipayPaymentService alipayPaymentService;
    @Mock
    private AlipayProperties alipayProperties;
    @Mock
    private OrderDelayProducer orderDelayProducer;
    @Mock
    private UserWalletService userWalletService;
    @Mock
    private MallOrderTimelineService mallOrderTimelineService;
    @Mock
    private MallOrderShippingService mallOrderShippingService;
    @Mock
    private MallCartService mallCartService;
    @Mock
    private UserAddressService userAddressService;
    @Mock
    private RedisCache redisCache;
    @Mock
    private MallProductSearchService mallProductSearchService;
    @Mock
    private MallOrderMapper mallOrderMapper;

    @Spy
    @InjectMocks
    private MallOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", mallOrderMapper);
    }

    @Test
    void getOrderDetail_WhenOrderDoesNotBelongToUser_ShouldThrowNotFound() {
        when(mallOrderMapper.getOrderDetailByOrderNo("O202511130001", 88L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.getOrderDetail("O202511130001", 88L));

        assertEquals(ResponseCode.RESULT_IS_NULL.getCode(), exception.getCode());
        assertEquals("订单不存在", exception.getMessage());
        verify(mallOrderMapper).getOrderDetailByOrderNo("O202511130001", 88L);
        verifyNoInteractions(mallOrderItemService, mallOrderShippingService);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getOrderShipping_ShouldScopeByUserAndParseNodes() {
        LambdaQueryChainWrapper<MallOrder> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(query).when(service).lambdaQuery();
        when(query.one()).thenReturn(createOrder(OrderStatusEnum.PENDING_RECEIPT.getType()));
        when(mallOrderShippingService.getByOrderId(1L)).thenReturn(MallOrderShipping.builder()
                .orderId(1L)
                .shippingCompany("顺丰")
                .shippingNo("SF1234567890")
                .status(ShippingStatusEnum.IN_TRANSIT.getType())
                .shippingInfo("[{\"time\":\"2025-11-13 12:00:00\",\"content\":\"快件已揽收\",\"location\":\"上海\"}]")
                .build());

        var result = service.getOrderShipping("O202511130001", 88L);

        assertNotNull(result);
        assertEquals("O202511130001", result.getOrderNo());
        assertEquals("顺丰", result.getLogisticsCompany());
        assertEquals(1, result.getNodes().size());
        assertEquals("上海", result.getNodes().getFirst().getLocation());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getOrderTimeline_ShouldMapTimelineForOwnedOrder() {
        LambdaQueryChainWrapper<MallOrder> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(query).when(service).lambdaQuery();
        when(query.one()).thenReturn(createOrder(OrderStatusEnum.PENDING_RECEIPT.getType()));

        MallOrderTimeline timeline = new MallOrderTimeline();
        timeline.setId(1L);
        timeline.setOrderId(1L);
        timeline.setEventType(OrderEventTypeEnum.ORDER_CREATED.getType());
        timeline.setEventStatus(OrderStatusEnum.PENDING_PAYMENT.getType());
        timeline.setOperatorType(OperatorTypeEnum.USER.getType());
        timeline.setDescription("用户创建订单");
        timeline.setCreatedTime(new Date());
        when(mallOrderTimelineService.getTimelineByOrderId(1L)).thenReturn(List.of(timeline));

        var result = service.getOrderTimeline("O202511130001", 88L);

        assertEquals("O202511130001", result.getOrderNo());
        assertEquals("待收货", result.getOrderStatusName());
        assertEquals(1, result.getTimeline().size());
        assertEquals("订单创建", result.getTimeline().getFirst().getEventTypeName());
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkOrderCancelable_WhenOrderDoesNotBelongToUser_ShouldReturnNotFoundResult() {
        LambdaQueryChainWrapper<MallOrder> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(query).when(service).lambdaQuery();
        when(query.one()).thenReturn(null);

        var result = service.checkOrderCancelable("O202511130001", 88L);

        assertFalse(result.getCancelable());
        assertEquals("ORDER_NOT_FOUND", result.getReasonCode());
        assertEquals("订单不存在或无权访问", result.getReasonMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkOrderCancelable_WhenPendingPayment_ShouldReturnCancelable() {
        LambdaQueryChainWrapper<MallOrder> query = mock(LambdaQueryChainWrapper.class, RETURNS_SELF);
        doReturn(query).when(service).lambdaQuery();
        when(query.one()).thenReturn(createOrder(OrderStatusEnum.PENDING_PAYMENT.getType()));

        var result = service.checkOrderCancelable("O202511130001", 88L);

        assertEquals(true, result.getCancelable());
        assertEquals("CAN_CANCEL", result.getReasonCode());
        assertEquals("待支付", result.getOrderStatusName());
    }

    private MallOrder createOrder(String orderStatus) {
        return MallOrder.builder()
                .id(1L)
                .orderNo("O202511130001")
                .userId(88L)
                .orderStatus(orderStatus)
                .deliveryType(DeliveryTypeEnum.EXPRESS.getType())
                .receiverName("张三")
                .receiverPhone("13800000000")
                .receiverDetail("上海市浦东新区")
                .totalAmount(new BigDecimal("59.80"))
                .build();
    }
}
