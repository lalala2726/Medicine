package cn.zhangchuangla.medicine.payment.service.impl;

import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.exception.AlipayPaymentException;
import cn.zhangchuangla.medicine.payment.model.AlipayPagePayRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 支付宝支付服务的默认实现。
 */
public class AlipayPaymentServiceImpl implements AlipayPaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlipayPaymentServiceImpl.class);

    private final AlipayClient alipayClient;
    private final AlipayProperties properties;

    public AlipayPaymentServiceImpl(AlipayClient alipayClient, AlipayProperties properties) {
        this.alipayClient = alipayClient;
        this.properties = properties;
    }

    @Override
    public String generatePagePayForm(AlipayPagePayRequest request) {
        Assert.notNull(request, "request 不能为空");
        validateRequiredParams(request);

        // notifyUrl 与 returnUrl 支持按请求覆盖，便于不同服务拥有独立的回调逻辑。
        String notifyUrl = determineUrl(request.getNotifyUrl(), properties.getNotifyUrl(), true);
        String returnUrl = determineUrl(request.getReturnUrl(), properties.getReturnUrl(), false);

        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        if (StringUtils.hasText(notifyUrl)) {
            payRequest.setNotifyUrl(notifyUrl);
        }
        if (StringUtils.hasText(returnUrl)) {
            payRequest.setReturnUrl(returnUrl);
        }

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setSubject(request.getSubject());
        model.setTotalAmount(request.getTotalAmount());
        model.setBody(request.getBody());
        model.setProductCode(request.getProductCode());
        model.setTimeoutExpress(request.getTimeoutExpress());
        payRequest.setBizModel(model);

        try {
            LOGGER.debug("调用支付宝下单：outTradeNo={}, subject={}", request.getOutTradeNo(), request.getSubject());
            AlipayTradePagePayResponse response = alipayClient.pageExecute(payRequest);
            if (!response.isSuccess()) {
                throw new AlipayPaymentException("调用支付宝支付失败：" + response.getSubMsg());
            }
            return response.getBody();
        } catch (AlipayApiException ex) {
            throw new AlipayPaymentException("调用支付宝支付接口异常", ex);
        }
    }

    private void validateRequiredParams(AlipayPagePayRequest request) {
        if (!StringUtils.hasText(request.getOutTradeNo())) {
            throw new AlipayPaymentException("outTradeNo（商户订单号）不能为空");
        }
        if (!StringUtils.hasText(request.getSubject())) {
            throw new AlipayPaymentException("subject（订单标题）不能为空");
        }
        if (!StringUtils.hasText(request.getTotalAmount())) {
            throw new AlipayPaymentException("totalAmount（订单金额）不能为空");
        }
    }

    private String determineUrl(String requestUrl, String defaultUrl, boolean required) {
        String resolved = StringUtils.hasText(requestUrl) ? requestUrl : defaultUrl;
        if (required && !StringUtils.hasText(resolved)) {
            throw new AlipayPaymentException("请配置 notifyUrl，必须要有一个可访问的异步通知地址");
        }
        return resolved;
    }
}
