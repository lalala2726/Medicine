package cn.zhangchuangla.medicine.payment.service.impl;

import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.exception.AlipayPaymentException;
import cn.zhangchuangla.medicine.payment.model.AlipayPagePayRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayQrCodeRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundVo;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

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
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("支付宝页面支付设置回调地址，notifyUrl={}, returnUrl={}", notifyUrl, returnUrl);
        }

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

    /**
     * 调用支付宝开放平台退款接口，封装参数校验、异常处理以及日志记录。
     */
    @Override
    public AlipayRefundVo refund(AlipayRefundRequest request) {
        Assert.notNull(request, "request 不能为空");
        validateRefundParams(request);

        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setTradeNo(request.getTradeNo());
        model.setRefundAmount(request.getRefundAmount());
        model.setRefundReason(request.getRefundReason());
        model.setOutRequestNo(request.getOutRequestNo());

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        refundRequest.setBizModel(model);

        try {
            LOGGER.debug("调用支付宝退款：outTradeNo={}, tradeNo={}", request.getOutTradeNo(), request.getTradeNo());
            AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
            if (!response.isSuccess()) {
                throw new AlipayPaymentException("调用支付宝退款失败：" + response.getSubMsg());
            }
            return AlipayRefundVo.builder()
                    .tradeNo(response.getTradeNo())
                    .outTradeNo(response.getOutTradeNo())
                    .buyerLogonId(response.getBuyerLogonId())
                    .refundFee(response.getRefundFee())
                    .fundChange("Y".equalsIgnoreCase(response.getFundChange()))
                    .gmtRefundPay(response.getGmtRefundPay())
                    .build();
        } catch (AlipayApiException ex) {
            throw new AlipayPaymentException("调用支付宝退款接口异常", ex);
        }
    }

    @Override
    public String generateQrCodeBase64(AlipayQrCodeRequest request) {
        Assert.notNull(request, "request 不能为空");
        validateQrCodeParams(request);

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(request.getOutTradeNo());
        model.setSubject(request.getSubject());
        model.setTotalAmount(request.getTotalAmount());
        model.setBody(request.getBody());
        model.setTimeoutExpress(request.getTimeoutExpress());

        AlipayTradePrecreateRequest precreateRequest = new AlipayTradePrecreateRequest();
        precreateRequest.setBizModel(model);
        String notifyUrl = request.getNotifyUrl();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("支付宝当面付预下单使用 notifyUrl={}", notifyUrl);
        }

        if (StringUtils.hasText(notifyUrl)) {
            precreateRequest.setNotifyUrl(notifyUrl);
        }

        try {
            LOGGER.debug("调用支付宝当面付预下单：outTradeNo={}, subject={}", request.getOutTradeNo(), request.getSubject());
            AlipayTradePrecreateResponse response = alipayClient.execute(precreateRequest);
            if (!response.isSuccess()) {
                throw new AlipayPaymentException("调用支付宝预下单失败：" + response.getSubMsg());
            }
            String qrCodeContent = response.getQrCode();
            if (!StringUtils.hasText(qrCodeContent)) {
                throw new AlipayPaymentException("支付宝返回的二维码内容为空");
            }
            return encodeQrCodeBase64(qrCodeContent, request.getQrCodeWidth(), request.getQrCodeHeight());
        } catch (AlipayApiException ex) {
            throw new AlipayPaymentException("调用支付宝预下单接口异常", ex);
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

    private void validateRefundParams(AlipayRefundRequest request) {
        boolean hasOutTradeNo = StringUtils.hasText(request.getOutTradeNo());
        boolean hasTradeNo = StringUtils.hasText(request.getTradeNo());
        if (!hasOutTradeNo && !hasTradeNo) {
            throw new AlipayPaymentException("outTradeNo 与 tradeNo 不能同时为空");
        }
        if (!StringUtils.hasText(request.getRefundAmount())) {
            throw new AlipayPaymentException("refundAmount（退款金额）不能为空");
        }
    }

    private void validateQrCodeParams(AlipayQrCodeRequest request) {
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
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("resolve alipay url -> requestUrl={}, defaultUrl={}, resolved={}, required={}",
                    requestUrl, defaultUrl, resolved, required);
        }
        if (required && !StringUtils.hasText(resolved)) {
            throw new AlipayPaymentException("请配置 notifyUrl，必须要有一个可访问的异步通知地址");
        }
        return resolved;
    }

    private String encodeQrCodeBase64(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(qrImage, "PNG", outputStream);
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            }
        } catch (WriterException | IOException ex) {
            throw new AlipayPaymentException("生成二维码图片失败", ex);
        }
    }
}
