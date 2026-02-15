package cn.zhangchuangla.medicine.agent.model.vo.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单趋势点。
 */
@Data
@Schema(description = "订单趋势点")
public class OrderTrendPoint {

    @Schema(description = "分组标签：按日/周/月")
    private String label;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "订单金额合计")
    private BigDecimal orderAmount;
}
