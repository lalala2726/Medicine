package cn.zhangchuangla.medicine.payment.service;

import cn.zhangchuangla.medicine.payment.model.AlipayPagePayRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayQrCodeRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundResult;

/**
 * 支付宝支付能力的核心出口。
 */
public interface AlipayPaymentService {

    /**
     * 生成支付宝电脑网站支付的 HTML 表单。
     * <p>
     * 该表单可以直接写入 HTTP 响应中，浏览器会自动跳转到支付宝收银台。
     * notifyUrl 和 returnUrl 可以由请求参数覆盖，以适配不同服务的回调地址。
     * </p>
     *
     * @param request 支付请求参数
     * @return 支付宝返回的 HTML 表单字符串
     */
    String generatePagePayForm(AlipayPagePayRequest request);

    /**
     * 调用支付宝退款接口。
     *
     * @param request 退款请求参数
     * @return 退款结果
     */
    AlipayRefundResult refund(AlipayRefundRequest request);

    /**
     * 生成支付宝当面付二维码图片，返回 Base64。
     *
     * @param request 二维码请求参数
     * @return Base64 编码的 PNG 图片
     */
    String generateQrCodeBase64(AlipayQrCodeRequest request);
}
