package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单支付信息响应。
 * <p>
 * 用于待支付订单重新拉起支付页面时展示，字段与 checkout 返回保持一致。
 * </p>
 */
@Data
@Builder
@Schema(description = "订单支付信息响应")
public class OrderPayInfoVo {

    @Schema(description = "订单号", example = "O20251113112233445566")
    private String orderNo;

    @Schema(description = "订单金额", example = "128.50")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态", example = "PENDING_PAYMENT")
    private String orderStatus;

    @Schema(description = "创建时间", example = "2025-11-13 10:00:00")
    private Date createTime;

    @Schema(description = "过期时间", example = "2025-11-13 10:30:00")
    private Date payExpireTime;

    @Schema(description = "商品摘要", example = "布洛芬缓释胶囊 x2 等2件")
    private String productSummary;

    @Schema(description = "商品种类数量", example = "2")
    private Integer itemCount;
}
