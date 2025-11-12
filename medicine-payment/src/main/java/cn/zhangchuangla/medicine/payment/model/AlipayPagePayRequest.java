package cn.zhangchuangla.medicine.payment.model;

import lombok.Builder;
import lombok.Data;

/**
 * 发起电脑网站支付时需要用到的核心入参。
 * <p>
 * 该对象与支付宝的 AlipayTradePagePayModel 一一对应，但保留了 notifyUrl、returnUrl，
 * 方便不同业务线根据自身部署地址灵活覆盖。
 * </p>
 */
@Data
@Builder
public class AlipayPagePayRequest {

    /**
     * 商户订单号，64 个字符以内。请保持在系统中唯一。
     */
    private String outTradeNo;

    /**
     * 订单标题，会展示在支付宝收银台上。
     */
    private String subject;

    /**
     * 订单总金额，单位为元，保留两位小数。
     */
    private String totalAmount;

    /**
     * 订单描述信息，可选。
     */
    private String body;

    /**
     * 产品码，电脑网站支付固定为 FAST_INSTANT_TRADE_PAY。
     */
    @Builder.Default
    private String productCode = "FAST_INSTANT_TRADE_PAY";

    /**
     * 订单超时时间，格式示例：30m、1h 等，可选。
     */
    private String timeoutExpress;

    /**
     * 针对当前业务的异步通知地址，不传时会回落到全局配置。
     */
    private String notifyUrl;

    /**
     * 针对当前业务的同步返回地址，不传时会回落到全局配置。
     */
    private String returnUrl;
}
