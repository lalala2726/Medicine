package cn.zhangchuangla.medicine.llm.model.tool.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单趋势点")
public class OrderTrendPointSnapshotTool {

    @Schema(description = "分组标签")
    private String label;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "订单金额合计")
    private BigDecimal orderAmount;
}
