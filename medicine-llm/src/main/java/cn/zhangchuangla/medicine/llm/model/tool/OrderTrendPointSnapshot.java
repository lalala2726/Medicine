package cn.zhangchuangla.medicine.llm.model.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "订单趋势点")
public class OrderTrendPointSnapshot {

    @Schema(description = "分组标签")
    private String label;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "订单金额合计")
    private BigDecimal orderAmount;
}
