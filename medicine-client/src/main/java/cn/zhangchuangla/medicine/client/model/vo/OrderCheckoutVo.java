package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单结算响应对象
 * <p>
 * 根据支付方式返回不同的数据结构:
 * - 钱包支付: 返回订单信息 + 支付成功标识
 * - 支付宝支付: 返回订单信息 + HTML表单字符串
 * </p>
 *
 * @author Chuang
 * created on 2025/11/12
 */
@Data
@Builder
@Schema(description = "订单结算响应对象")
public class OrderCheckoutVo {

    @Schema(description = "订单号", example = "O2025103011223344")
    private String orderNo;

    @Schema(description = "订单金额", example = "128.50")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态", example = "WaitPay")
    private String orderStatus;

    @Schema(description = "创建时间", example = "2025-10-30 13:22:33")
    private Date createTime;

    @Schema(description = "过期时间", example = "2025-10-30 13:37:33")
    private Date expireTime;

    @Schema(description = "商品摘要", example = "复方感冒灵颗粒 x2")
    private String productSummary;

    @Schema(description = "商品种类数量", example = "3")
    private Integer itemCount;

    @Schema(description = "支付方式", example = "WALLET")
    private String paymentMethod;

    @Schema(description = "支付状态: SUCCESS-支付成功, PENDING-待支付", example = "SUCCESS")
    private String paymentStatus;

    @Schema(description = "支付数据: 钱包支付为null, 支付宝支付为HTML表单字符串", example = "<form>...</form>")
    private String paymentData;
}

