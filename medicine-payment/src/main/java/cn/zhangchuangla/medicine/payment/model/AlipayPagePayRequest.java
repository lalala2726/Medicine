package cn.zhangchuangla.medicine.payment.model;

import lombok.Data;

/**
 * 发起电脑网站支付时需要用到的核心入参。
 * <p>
 * 该对象与支付宝的 AlipayTradePagePayModel 一一对应，但保留了 notifyUrl、returnUrl，
 * 方便不同业务线根据自身部署地址灵活覆盖。
 * </p>
 */
@Data
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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 手写一个简单的建造者用来组装请求参数，方便调用方使用链式写法。
     */
    public static final class Builder {
        private final AlipayPagePayRequest target = new AlipayPagePayRequest();

        private Builder() {
        }

        public Builder outTradeNo(String outTradeNo) {
            target.setOutTradeNo(outTradeNo);
            return this;
        }

        public Builder subject(String subject) {
            target.setSubject(subject);
            return this;
        }

        public Builder totalAmount(String totalAmount) {
            target.setTotalAmount(totalAmount);
            return this;
        }

        public Builder body(String body) {
            target.setBody(body);
            return this;
        }

        public Builder productCode(String productCode) {
            target.setProductCode(productCode);
            return this;
        }

        public Builder timeoutExpress(String timeoutExpress) {
            target.setTimeoutExpress(timeoutExpress);
            return this;
        }

        public Builder notifyUrl(String notifyUrl) {
            target.setNotifyUrl(notifyUrl);
            return this;
        }

        public Builder returnUrl(String returnUrl) {
            target.setReturnUrl(returnUrl);
            return this;
        }

        public AlipayPagePayRequest build() {
            return target;
        }
    }
}
