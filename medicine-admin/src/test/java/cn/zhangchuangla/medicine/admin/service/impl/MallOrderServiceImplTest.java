package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.admin.mapper.UserMapper;
import cn.zhangchuangla.medicine.admin.model.request.OrderRefundRequest;
import cn.zhangchuangla.medicine.admin.service.MallOrderItemService;
import cn.zhangchuangla.medicine.admin.service.MallProductImageService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import cn.zhangchuangla.medicine.model.enums.PayTypeEnum;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MallOrderServiceImplTest {

    @Mock
    private MallOrderMapper mallOrderMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MallOrderItemService mallOrderItemService;
    @Mock
    private MallProductImageService mallProductImageService;
    @Mock
    private AlipayPaymentService alipayPaymentService;

    private MallOrderServiceImpl mallOrderService;

    @BeforeEach
    void setUp() {
        MallOrderServiceImpl target = new MallOrderServiceImpl(mallOrderMapper, userMapper, mallOrderItemService, mallProductImageService, alipayPaymentService);
        // 使用 Spy 拦截持久层调用，方便断言订单对象的最终状态。
        this.mallOrderService = Mockito.spy(target);
    }

    @Test
    void orderRefund_shouldInvokeAlipayAndRecordPartialRefund() {
        MallOrder mallOrder = buildOrder(PayTypeEnum.ALIPAY.getType(), BigDecimal.valueOf(200), BigDecimal.valueOf(50));
        OrderRefundRequest request = buildRefundRequest(mallOrder.getOrderNo(), BigDecimal.valueOf(100), "用户申请部分退款");

        doReturn(mallOrder).when(mallOrderService).getOrderByOrderNo(mallOrder.getOrderNo());
        doReturn(true).when(mallOrderService).updateById(any(MallOrder.class));

        boolean success = mallOrderService.orderRefund(request);

        assertThat(success).isTrue();

        ArgumentCaptor<AlipayRefundRequest> captor = ArgumentCaptor.forClass(AlipayRefundRequest.class);
        verify(alipayPaymentService, times(1)).refund(captor.capture());
        AlipayRefundRequest alipayRefundRequest = captor.getValue();
        assertThat(alipayRefundRequest.getOutTradeNo()).isEqualTo(mallOrder.getOrderNo());
        assertThat(alipayRefundRequest.getRefundAmount()).isEqualTo("100.00");
        assertThat(alipayRefundRequest.getRefundReason()).isEqualTo("用户申请部分退款");
        assertThat(alipayRefundRequest.getOutRequestNo()).startsWith(mallOrder.getOrderNo() + "-REFUND-");

        assertThat(mallOrder.getRefundPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(mallOrder.getRefundStatus()).isEqualTo("PARTIAL");
        assertThat(mallOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.AFTER_SALE.getType());
        assertThat(mallOrder.getRefundTime()).isNotNull();
        verify(mallOrderService).updateById(mallOrder);
    }

    @Test
    void orderRefund_shouldMarkOrderFullyRefundedWhenAmountMatchesPayAmount() {
        MallOrder mallOrder = buildOrder(PayTypeEnum.ALIPAY.getType(), BigDecimal.valueOf(150), BigDecimal.ZERO);
        OrderRefundRequest request = buildRefundRequest(mallOrder.getOrderNo(), BigDecimal.valueOf(150), null);

        doReturn(mallOrder).when(mallOrderService).getOrderByOrderNo(mallOrder.getOrderNo());
        doReturn(true).when(mallOrderService).updateById(any(MallOrder.class));

        mallOrderService.orderRefund(request);

        ArgumentCaptor<AlipayRefundRequest> captor = ArgumentCaptor.forClass(AlipayRefundRequest.class);
        verify(alipayPaymentService).refund(captor.capture());
        assertThat(captor.getValue().getRefundReason()).isEqualTo("管理员发起退款");

        assertThat(mallOrder.getRefundPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(mallOrder.getRefundStatus()).isEqualTo("SUCCESS");
        assertThat(mallOrder.getOrderStatus()).isEqualTo(OrderStatusEnum.REFUNDED.getType());
        assertThat(mallOrder.getRefundTime()).isNotNull();
        verify(mallOrderService).updateById(mallOrder);
    }

    @Test
    void orderRefund_shouldRejectWhenOrderNotPaid() {
        MallOrder mallOrder = buildOrder(PayTypeEnum.ALIPAY.getType(), BigDecimal.valueOf(100), BigDecimal.ZERO);
        mallOrder.setPaid(0);
        OrderRefundRequest request = buildRefundRequest(mallOrder.getOrderNo(), BigDecimal.valueOf(50), "未支付退款");

        doReturn(mallOrder).when(mallOrderService).getOrderByOrderNo(mallOrder.getOrderNo());

        assertThatThrownBy(() -> mallOrderService.orderRefund(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单未支付");

        verify(alipayPaymentService, never()).refund(any());
        verify(mallOrderService, never()).updateById(any());
    }

    @Test
    void orderRefund_shouldRejectWhenRefundAmountExceedsAllowed() {
        MallOrder mallOrder = buildOrder(PayTypeEnum.ALIPAY.getType(), BigDecimal.valueOf(100), BigDecimal.valueOf(80));
        OrderRefundRequest request = buildRefundRequest(mallOrder.getOrderNo(), BigDecimal.valueOf(30), "超额退款");

        doReturn(mallOrder).when(mallOrderService).getOrderByOrderNo(mallOrder.getOrderNo());

        assertThatThrownBy(() -> mallOrderService.orderRefund(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("退款金额不能大于可退款金额");

        verify(alipayPaymentService, never()).refund(any());
        verify(mallOrderService, never()).updateById(any());
    }

    @Test
    void orderRefund_shouldRejectUnsupportedPayType() {
        MallOrder mallOrder = buildOrder(PayTypeEnum.WECHAT_PAY.getType(), BigDecimal.valueOf(120), BigDecimal.ZERO);
        OrderRefundRequest request = buildRefundRequest(mallOrder.getOrderNo(), BigDecimal.valueOf(60), "不支持的渠道");

        doReturn(mallOrder).when(mallOrderService).getOrderByOrderNo(mallOrder.getOrderNo());

        assertThatThrownBy(() -> mallOrderService.orderRefund(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("暂不支持该支付方式退款");

        verify(alipayPaymentService, never()).refund(any());
        verify(mallOrderService, never()).updateById(any());
    }

    private MallOrder buildOrder(String payType, BigDecimal payAmount, BigDecimal refundPrice) {
        return MallOrder.builder()
                .orderNo("ORDER-" + System.nanoTime())
                .payType(payType)
                .payAmount(payAmount)
                .refundPrice(refundPrice)
                .paid(1)
                .orderStatus(OrderStatusEnum.PENDING_SHIPMENT.getType())
                .build();
    }

    private OrderRefundRequest buildRefundRequest(String orderNo, BigDecimal refundAmount, String reason) {
        OrderRefundRequest request = new OrderRefundRequest();
        request.setOrderNo(orderNo);
        request.setRefundAmount(refundAmount);
        request.setRefundReason(reason);
        return request;
    }
}
