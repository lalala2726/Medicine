package cn.zhangchuangla.medicine.payment.model;

import lombok.Builder;
import lombok.Data;

/**
 * 支付宝二维码支付请求参数。
 */
@Data
@Builder
public class AlipayQrCodeRequest {

    /**
     * 商户订单号，需保证系统中唯一。
     */
    private String outTradeNo;

    /**
     * 支付主题，将展示在支付宝支付页面。
     */
    private String subject;

    /**
     * 订单总金额，单位元，保留两位小数。
     */
    private String totalAmount;

    /**
     * 支付描述，可选。
     */
    private String body;

    /**
     * 支付超时时间，如 30m。
     */
    private String timeoutExpress;

    /**
     * 支付宝异步通知地址。
     */
    private String notifyUrl;

    /**
     * 生成二维码的宽度，默认 256。
     */
    @Builder.Default
    private int qrCodeWidth = 256;

    /**
     * 生成二维码的高度，默认 256。
     */
    @Builder.Default
    private int qrCodeHeight = 256;
}
