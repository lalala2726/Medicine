package cn.zhangchuangla.medicine.payment.model;

import lombok.Builder;
import lombok.Data;

/**
 * 支付宝退款请求入参。
 */
@Data
@Builder
public class AlipayRefundRequest {

    /**
     * 商户订单号。tradeNo 与 outTradeNo 不能同时为空。
     */
    private String outTradeNo;

    /**
     * 支付宝交易号。tradeNo 与 outTradeNo 不能同时为空。
     */
    private String tradeNo;

    /**
     * 退款金额，单位元，支持两位小数。
     */
    private String refundAmount;

    /**
     * 退款原因说明。
     */
    private String refundReason;

    /**
     * 标识一次退款请求的唯一 key，用于部分退款或幂等控制。
     */
    private String outRequestNo;
}
