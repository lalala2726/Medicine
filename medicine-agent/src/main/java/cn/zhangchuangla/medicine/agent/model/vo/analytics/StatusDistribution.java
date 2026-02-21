package cn.zhangchuangla.medicine.agent.model.vo.analytics;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentCodePair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单状态分布。
 */
@Data
@Schema(description = "订单状态分布")
public class StatusDistribution {

    @Schema(description = "订单状态（value-编码，description-描述）")
    @AgentCodeLabel(
            pairs = {
                    @AgentCodePair(code = "PENDING_PAYMENT", label = "待支付"),
                    @AgentCodePair(code = "PENDING_SHIPMENT", label = "待发货"),
                    @AgentCodePair(code = "PENDING_RECEIPT", label = "待收货"),
                    @AgentCodePair(code = "COMPLETED", label = "已完成"),
                    @AgentCodePair(code = "REFUNDED", label = "已退款"),
                    @AgentCodePair(code = "AFTER_SALE", label = "售后中"),
                    @AgentCodePair(code = "EXPIRED", label = "已过期"),
                    @AgentCodePair(code = "CANCELLED", label = "已取消")
            }
    )
    private String status;

    @Schema(description = "订单状态名称")
    private String statusName;

    @Schema(description = "数量")
    private Long count;
}
