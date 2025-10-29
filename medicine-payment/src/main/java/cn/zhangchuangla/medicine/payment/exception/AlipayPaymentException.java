package cn.zhangchuangla.medicine.payment.exception;

/**
 * 自定义的支付宝支付异常，便于业务层捕获与定位问题。
 */
public class AlipayPaymentException extends RuntimeException {

    public AlipayPaymentException(String message) {
        super(message);
    }

    public AlipayPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
