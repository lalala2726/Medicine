package cn.zhangchuangla.medicine.llm.model.tool.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "运营总览快照")
public class AnalyticsOverviewSnapshot {

    @Schema(description = "用户总数")
    private Long totalUsers;

    @Schema(description = "订单总数")
    private Long totalOrders;

    @Schema(description = "已支付订单数")
    private Long paidOrders;

    @Schema(description = "售后/退款订单数")
    private Long refundCount;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "平均订单金额")
    private BigDecimal averageAmount;

    @Schema(description = "累计退款金额")
    private BigDecimal refundAmount;
}
