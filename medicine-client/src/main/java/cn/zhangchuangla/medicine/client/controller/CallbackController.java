package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.AlipayNotifyDTO;
import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.model.AlipayQrCodeRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/20
 */
@RestController
@RequestMapping("/callback")
@Slf4j
@Tag(name = "第三方服务回调接口", description = "回调接口")
public class CallbackController extends BaseController {

    private final AlipayProperties alipayProperties;
    private final AlipayPaymentService alipayPaymentService;
    private final MallOrderService mallOrderService;

    public CallbackController(AlipayProperties alipayProperties, AlipayPaymentService alipayPaymentService, MallOrderService mallOrderService) {
        this.alipayProperties = alipayProperties;
        this.alipayPaymentService = alipayPaymentService;
        this.mallOrderService = mallOrderService;
    }

    /**
     * 生成支付宝支付二维码（Base64 图片）。
     * <p>
     * 前端传入订单号后，服务端校验订单状态并调用支付宝当面付生成二维码，
     * 以 Base64 字符串形式返回，前端可直接渲染为图片。
     * </p>
     *
     * @param orderNo 订单编号
     * @return Base64 编码的 PNG 图片
     */
    @GetMapping("/alipay/pay-code")
    @Operation(summary = "生成支付宝支付二维码")
    public AjaxResult<String> generatePayQrCode(@RequestParam String orderNo) {
        // 查询订单详情
        OrderDetailVo orderDetail = mallOrderService.getOrderDetail(orderNo);
        BigDecimal amount = orderDetail.getTotalAmount();
        if (amount == null) {
            return error("订单金额缺失，无法发起支付");
        }

        String totalAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        String subject = "商城订单支付-" + orderNo;

        String notifyUrl = alipayProperties.getNotifyUrl();
        AlipayQrCodeRequest qrCodeRequest = AlipayQrCodeRequest.builder()
                .outTradeNo(orderNo)
                .subject(subject)
                .totalAmount(totalAmount)
                .body("商城订单支付")
                .notifyUrl(notifyUrl)
                .build();
        String base64 = alipayPaymentService.generateQrCodeBase64(qrCodeRequest);
        String dataUri = "data:image/png;base64," + base64;
        return success(dataUri);
    }

    /**
     * 支付宝异步通知回调。
     * <p>
     * 支付宝以 POST form 的方式推送支付结果，需要先验签再更新业务订单状态。
     * 验签通过后必须返回字符串 {@code success}，否则支付宝会不断重试通知。
     * </p>
     *
     * @param request 支付宝通知请求
     * @return 响应字符串
     */
    @PostMapping("/alipay/notify")
    @Operation(summary = "支付宝异步通知回调")
    @Anonymous
    public String handleAlipayNotify(AlipayNotifyDTO alipayNotifyDTO, HttpServletRequest request) {
        return mallOrderService.alipayNotify(alipayNotifyDTO, request);
    }

    /**
     * 支付宝同步回调（前端回跳）。
     * <p>
     * 用户从支付宝返回时走此接口，同样需要进行验签并给出友好提示。
     * </p>
     *
     * @param params 回调参数
     * @return 页面提示信息
     */
    @GetMapping("/alipay/return")
    @Operation(summary = "支付宝同步回调")
    public String handleAlipayReturn(@RequestParam Map<String, String> params) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                log.warn("支付宝同步回跳验签失败，参数：{}", params);
                return "验签失败，请检查支付结果";
            }
        } catch (AlipayApiException ex) {
            log.error("支付宝同步回跳验签异常", ex);
            return "验签出现异常，请稍后重试";
        }

        String outTradeNo = params.getOrDefault("out_trade_no", "未知订单");
        String tradeNo = params.getOrDefault("trade_no", "未知交易号");
        log.info("用户支付完成回跳，订单号：{}，支付宝交易号：{}", outTradeNo, tradeNo);
        return "支付完成，订单号：" + outTradeNo + "，支付宝交易号：" + tradeNo;
    }

}
