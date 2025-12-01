package cn.zhangchuangla.medicine.llm.model.tool.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单整体运营概况。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单整体概况")
public class OrderOverviewSnapshotTool {

    @Schema(description = "订单总量")
    private long totalOrders;

    @Schema(description = "待支付数量")
    private long pendingPayment;

    @Schema(description = "待发货数量")
    private long pendingShipment;

    @Schema(description = "待收货数量")
    private long pendingReceipt;

    @Schema(description = "已完成数量")
    private long completed;

    @Schema(description = "退款单数量（订单维度）")
    private long refunded;

    @Schema(description = "售后中数量")
    private long afterSale;

    @Schema(description = "已取消数量")
    private long cancelled;

    @Schema(description = "已支付金额合计")
    private BigDecimal totalSales;

    @Schema(description = "退款金额合计")
    private BigDecimal refundedAmount;
}
