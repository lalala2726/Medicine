package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.model.AlipayPagePayRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端用于演示支付宝网页支付的控制器。
 * <p>
 * 这里提供了三个接口：
 * <ol>
 *     <li>/alipay/test/pay：生成一个沙箱订单并跳转至支付宝收银台</li>
 *     <li>/alipay/notify：接收支付宝的异步通知（沙箱环境会回调）</li>
 *     <li>/alipay/return：接收支付宝的同步回跳并输出简单提示</li>
 * </ol>
 * 所有重要代码都附带中文说明，方便首次接入时逐行理解。
 * </p>
 */
@RestController
@Anonymous
@RequestMapping("/alipay")
public class PaymentTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentTestController.class);
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AlipayPaymentService alipayPaymentService;
    private final AlipayProperties alipayProperties;

    public PaymentTestController(AlipayPaymentService alipayPaymentService,
                                 AlipayProperties alipayProperties) {
        this.alipayPaymentService = alipayPaymentService;
        this.alipayProperties = alipayProperties;
    }

    /**
     * 生成一个测试订单并返回支付宝提供的 HTML 表单。
     * <p>
     * 1. 我们使用当前时间拼出一个唯一的商户订单号，方便在沙箱后台排查；
     * 2. notifyUrl / returnUrl 在这里手动覆盖，全局默认值在 medicine-payment 模块中配置，
     * 但由于 admin、client 是两个独立服务，所以各自的回调地址可以在代码里定制；
     * 3. 将生成的表单直接写入 HTTP 响应，浏览器会自动跳转到支付宝收银台。
     * </p>
     */
    @GetMapping("/test/pay")
    public void createSandboxOrder(@RequestParam(value = "amount", required = false, defaultValue = "0.1") String amount, HttpServletResponse response) throws IOException {
        String orderNo = "ADMIN-DEMO-" + ORDER_TIME_FORMATTER.format(LocalDateTime.now());

        AlipayPagePayRequest payRequest = AlipayPagePayRequest.builder()
                .outTradeNo(orderNo)
                .subject("管理端测试支付订单")
                .totalAmount(amount)
                .body("这是一笔用于验证沙箱环境配置的测试订单")
                // 根据当前服务的部署地址覆盖回调地址，避免多个系统之间互相干扰。
                .notifyUrl("http://medicine.zhangchuangla.cn/alipay/notify")
                .returnUrl("http://medicine.zhangchuangla.cn/alipay/return")
                .build();

        String form = alipayPaymentService.generatePagePayForm(payRequest);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.flushBuffer();

        LOGGER.info("已生成测试订单，订单号：{}", orderNo);
    }

    /**
     * 处理支付宝的异步通知。
     * <p>
     * 支付宝会以 POST form 的形式回调我们配置的 notifyUrl。
     * 沙箱环境只要确保外网可以访问到该地址，就会收到通知。
     * 验签通过后必须返回字符串 "success"，否则支付宝会继续重试通知。
     * </p>
     */
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                LOGGER.warn("支付宝异步通知验签失败，参数：{}", params);
                return "failure";
            }

            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            LOGGER.info("收到支付宝异步通知，订单号：{}，交易状态：{}", outTradeNo, tradeStatus);

            // 在实际业务中，这里需要根据 out_trade_no 更新订单状态。
            return "success";
        } catch (AlipayApiException ex) {
            LOGGER.error("支付宝异步通知验签异常", ex);
            return "failure";
        }
    }

    /**
     * 处理支付宝的同步回跳。
     * <p>
     * 用户支付完成后会回到我们配置的 returnUrl。
     * 这里同样进行验签，并输出一个简单的提示信息。
     * </p>
     */
    @GetMapping("/return")
    public String handleReturn(@RequestParam Map<String, String> params) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                LOGGER.warn("支付宝同步回跳验签失败，参数：{}", params);
                return "验签失败，请检查支付结果";
            }
        } catch (AlipayApiException ex) {
            LOGGER.error("支付宝同步回跳验签异常", ex);
            return "验签出现异常，请稍后重试";
        }

        String outTradeNo = params.getOrDefault("out_trade_no", "未知订单");
        String tradeNo = params.getOrDefault("trade_no", "未知交易号");
        LOGGER.info("用户从支付宝回到系统，订单号：{}，支付宝交易号：{}", outTradeNo, tradeNo);
        return "支付完成，订单号：" + outTradeNo + "，支付宝交易号：" + tradeNo;
    }

    /**
     * 将 request 中的参数转换为简单的 Map，便于验签。
     */
    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }
}
