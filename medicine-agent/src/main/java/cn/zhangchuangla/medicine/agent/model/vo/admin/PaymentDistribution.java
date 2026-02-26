package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentFieldDesc;
import cn.zhangchuangla.medicine.agent.annotation.AgentVoDesc;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付方式分布。
 */
@Data
@Schema(description = "支付方式分布")
@AgentVoDesc("支付方式分布")
public class PaymentDistribution {

    @Schema(description = "支付方式")
    @AgentFieldDesc("支付方式")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_ORDER_PAY_TYPE)
    private String payType;

    @Schema(description = "支付方式名称")
    @AgentFieldDesc("支付方式名称")
    private String payTypeName;

    @Schema(description = "订单数量")
    @AgentFieldDesc("订单数量")
    private Long count;

    @Schema(description = "金额合计")
    @AgentFieldDesc("金额合计")
    private BigDecimal amount;
}
