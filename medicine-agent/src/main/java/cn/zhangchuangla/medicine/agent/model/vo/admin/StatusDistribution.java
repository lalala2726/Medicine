package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单状态分布。
 */
@Data
@Schema(description = "订单状态分布")
public class StatusDistribution {

    @Schema(description = "订单状态（value-编码，description-描述）")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_STATUS)
    private String status;

    @Schema(description = "订单状态名称")
    private String statusName;

    @Schema(description = "数量")
    private Long count;
}
