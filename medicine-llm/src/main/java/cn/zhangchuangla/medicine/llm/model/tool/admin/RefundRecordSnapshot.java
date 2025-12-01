package cn.zhangchuangla.medicine.llm.model.tool.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 售后/退款记录的精简视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退款记录快照")
public class RefundRecordSnapshot {

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "售后类型")
    private String afterSaleType;

    @Schema(description = "售后状态")
    private String afterSaleStatus;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "申请时间")
    private Date applyTime;

    @Schema(description = "完成时间")
    private Date completeTime;

    @Schema(description = "用户申请原因")
    private String applyReason;
}
