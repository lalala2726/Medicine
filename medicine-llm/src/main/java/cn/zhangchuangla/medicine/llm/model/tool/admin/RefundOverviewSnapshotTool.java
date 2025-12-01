package cn.zhangchuangla.medicine.llm.model.tool.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后/退款总览数据。
 */
@Data
@Schema(description = "退款/售后总览")
public class RefundOverviewSnapshotTool {

    @Schema(description = "待审核数量")
    private long pending;

    @Schema(description = "处理中数量")
    private long processing;

    @Schema(description = "已完成数量")
    private long completed;

    @Schema(description = "已拒绝数量")
    private long rejected;

    @Schema(description = "累计申请退款金额")
    private BigDecimal totalRequestedAmount;

    @Schema(description = "实际已退款金额（订单维度）")
    private BigDecimal refundedAmount;

    @Schema(description = "最近的售后记录")
    private List<RefundRecordSnapshotTool> recentRecords;
}
