package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.request.OrderConfirmRequest;
import cn.zhangchuangla.medicine.client.model.request.OrderCreateRequest;
import cn.zhangchuangla.medicine.client.model.vo.OrderCreateVo;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 订单领域对外接口。
 *
 * <p>
 * 本控制器聚合了客户端订单创建、支付信息查询以及支付宝回调处理，
 * 方便第一次接入支付宝时快速理解整条业务链路。
 * </p>
 * <p>
 * created on 2025/10/31 01:33
 */
@Slf4j
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "订单管理")
public class OrderController extends BaseController {

    private final MallOrderService mallOrderService;
    private final AlipayProperties alipayProperties;
    private final AlipayPaymentService alipayPaymentService;

    public OrderController(MallOrderService mallOrderService,
                           AlipayProperties alipayProperties,
                           AlipayPaymentService alipayPaymentService) {
        this.mallOrderService = mallOrderService;
        this.alipayProperties = alipayProperties;
        this.alipayPaymentService = alipayPaymentService;
    }

    /**
     * 创建订单。
     * <p>
     * 校验前端传入的商品与数量，落库存、生成订单号后返回待支付信息。
     * </p>
     *
     * @param request 创建订单请求
     * @return 创建订单结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public AjaxResult<OrderCreateVo> createOrder(@Validated @RequestBody OrderCreateRequest request) {
        OrderCreateVo orderCreateVo = mallOrderService.createOrder(request);
        return success(orderCreateVo);
    }

    /**
     * 确认订单时，需要传入订单编号，服务端会校验订单状态并返回确认信息。
     *
     * @param request 确认订单请求
     * @return 如果是支付宝等第三方支付方式这边将返回对应的支付表单信息
     */
    @PostMapping("/confirm")
    @Operation(summary = "确认订单")
    public AjaxResult<?> confirmOrder(@Validated @RequestBody OrderConfirmRequest request) {
        String data = mallOrderService.confirmOrder(request);
        return success(data);
    }

    /**
     * 查询订单支付信息。
     * <p>
     * 前端在下单成功后、真正唤起支付宝前可以再次确认金额与状态，避免重复支付。
     * </p>
     *
     * @param orderNo 订单编号
     * @return 支付信息
     */
    @GetMapping("/pay-info/{orderNo}")
    @Operation(summary = "查询订单支付信息")
    public AjaxResult<OrderCreateVo> getOrderPayInfo(@PathVariable String orderNo) {
        OrderCreateVo payInfo = mallOrderService.getOrderPayInfo(orderNo);
        return success(payInfo);
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
        OrderCreateVo payInfo = mallOrderService.getOrderPayInfo(orderNo);
        BigDecimal amount = payInfo.getTotalAmount();
        if (amount == null) {
            return error("订单金额缺失，无法发起支付");
        }

        String totalAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        String subject = payInfo.getProductSummary();
        if (!StringUtils.hasText(subject)) {
            subject = "商城订单支付-" + orderNo;
        }
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
