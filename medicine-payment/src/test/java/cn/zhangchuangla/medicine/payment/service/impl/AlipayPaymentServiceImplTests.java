package cn.zhangchuangla.medicine.payment.service.impl;

import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.exception.AlipayPaymentException;
import cn.zhangchuangla.medicine.payment.model.AlipayPagePayRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import com.alipay.api.*;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlipayPaymentServiceImplTests {

    private FakeAlipayClient alipayClient;

    private AlipayProperties alipayProperties;

    private AlipayPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        alipayClient = new FakeAlipayClient();
        alipayProperties = new AlipayProperties();
        service = new AlipayPaymentServiceImpl(alipayClient, alipayProperties);
    }

    /**
     * 测试目的：验证页面支付请求会优先使用调用方覆盖的回调地址，并返回支付宝表单。
     * 测试结果：支付宝请求中的 notifyUrl、returnUrl 与业务模型字段均被正确组装。
     */
    @Test
    void generatePagePayForm_WhenRequestOverridesUrls_ShouldUseRequestUrlsAndReturnFormBody() {
        alipayProperties.setNotifyUrl("https://default-notify.example.com");
        alipayProperties.setReturnUrl("https://default-return.example.com");
        AlipayTradePagePayResponse response = new AlipayTradePagePayResponse();
        response.setCode("10000");
        response.setBody("<form>pay</form>");
        alipayClient.pagePayResponse = response;

        AlipayPagePayRequest request = AlipayPagePayRequest.builder()
                .outTradeNo("ORDER001")
                .subject("订单支付")
                .totalAmount("88.00")
                .body("订单支付说明")
                .timeoutExpress("30m")
                .notifyUrl("https://request-notify.example.com")
                .returnUrl("https://request-return.example.com")
                .build();

        String result = service.generatePagePayForm(request);

        // 测试结果：返回支付宝表单，并且使用请求级别回调地址覆盖默认配置。
        assertEquals("<form>pay</form>", result);
        assertEquals("https://request-notify.example.com", alipayClient.lastPagePayRequest.getNotifyUrl());
        assertEquals("https://request-return.example.com", alipayClient.lastPagePayRequest.getReturnUrl());
        Object actualBizModel = alipayClient.lastPagePayRequest.getBizModel();
        assertTrue(actualBizModel instanceof AlipayTradePagePayModel);
        AlipayTradePagePayModel actualModel = (AlipayTradePagePayModel) actualBizModel;
        assertEquals("ORDER001", actualModel.getOutTradeNo());
        assertEquals("88.00", actualModel.getTotalAmount());
    }

    /**
     * 测试目的：验证支付宝下单返回失败状态时，会抛出明确的支付异常。
     * 测试结果：异常信息包含支付宝返回的失败原因，方便业务侧定位问题。
     */
    @Test
    void generatePagePayForm_WhenAlipayReturnsFailure_ShouldThrowPaymentException() {
        alipayProperties.setNotifyUrl("https://notify.example.com");
        AlipayTradePagePayResponse response = new AlipayTradePagePayResponse() {
            @Override
            public boolean isSuccess() {
                return false;
            }
        };
        response.setSubMsg("系统繁忙");
        alipayClient.pagePayResponse = response;

        AlipayPagePayRequest request = AlipayPagePayRequest.builder()
                .outTradeNo("ORDER001")
                .subject("订单支付")
                .totalAmount("88.00")
                .build();

        AlipayPaymentException exception = assertThrows(AlipayPaymentException.class, () -> service.generatePagePayForm(request));

        // 测试结果：失败信息会透传到业务异常中。
        assertEquals("调用支付宝支付失败：系统繁忙", exception.getMessage());
    }

    /**
     * 测试目的：验证退款请求在商户订单号和支付宝交易号都为空时会被前置拦截。
     * 测试结果：服务直接抛出参数校验异常，避免继续调用外部支付宝接口。
     */
    @Test
    void refund_WhenTradeIdentifiersAreMissing_ShouldThrowValidationException() {
        AlipayRefundRequest request = AlipayRefundRequest.builder()
                .refundAmount("18.00")
                .build();

        AlipayPaymentException exception = assertThrows(AlipayPaymentException.class, () -> service.refund(request));

        // 测试结果：参数校验失败时不会继续发起支付宝退款请求。
        assertEquals("outTradeNo 与 tradeNo 不能同时为空", exception.getMessage());
    }

    /**
     * 支付宝客户端测试替身，仅实现当前单元测试需要的方法。
     */
    private static final class FakeAlipayClient implements AlipayClient {

        /**
         * 最近一次页面支付请求。
         */
        private AlipayTradePagePayRequest lastPagePayRequest;

        /**
         * 预设的页面支付响应。
         */
        private AlipayTradePagePayResponse pagePayResponse;

        /**
         * 预设的退款响应。
         */
        private AlipayTradeRefundResponse refundResponse;

        @Override
        @SuppressWarnings("unchecked")
        public <T extends AlipayResponse> T execute(AlipayRequest<T> request) throws AlipayApiException {
            if (request instanceof AlipayTradeRefundRequest) {
                return (T) refundResponse;
            }
            throw new UnsupportedOperationException("当前测试替身仅支持退款请求");
        }

        @Override
        public <T extends AlipayResponse> T execute(AlipayRequest<T> request, String authToken) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T execute(AlipayRequest<T> request, String authToken, String appAuthToken) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T execute(AlipayRequest<T> request, String authToken, String appAuthToken, String targetAppId) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends AlipayResponse> T pageExecute(AlipayRequest<T> request) throws AlipayApiException {
            lastPagePayRequest = (AlipayTradePagePayRequest) request;
            return (T) pagePayResponse;
        }

        @Override
        public <T extends AlipayResponse> T sdkExecute(AlipayRequest<T> request) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T pageExecute(AlipayRequest<T> request, String httpMethod) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <TR extends AlipayResponse, T extends AlipayRequest<TR>> TR parseAppSyncResult(Map<String, String> resultMap,
                                                                                              Class<T> clazz) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public BatchAlipayResponse execute(BatchAlipayRequest request) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request, String authToken) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request, String authToken, String appAuthToken) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }

        @Override
        public <T extends AlipayResponse> T certificateExecute(AlipayRequest<T> request,
                                                               String authToken,
                                                               String appAuthToken,
                                                               String targetAppId) {
            throw new UnsupportedOperationException("当前测试未使用该方法");
        }
    }
}
