package cn.zhangchuangla.medicine.agent.model.vo.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单状态分布。
 */
@Data
@Schema(description = "订单状态分布")
public class StatusDistribution {

    @Schema(description = "订单状态编码")
    private String status;

    @Schema(description = "订单状态名称")
    private String statusName;

    @Schema(description = "数量")
    private Long count;
}
