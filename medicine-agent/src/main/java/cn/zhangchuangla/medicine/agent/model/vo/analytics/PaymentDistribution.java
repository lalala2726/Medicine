package cn.zhangchuangla.medicine.agent.model.vo.analytics;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.AgentCodePair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付方式分布。
 */
@Data
@Schema(description = "支付方式分布")
public class PaymentDistribution {

    @Schema(description = "支付方式（value-编码，description-描述）")
    @AgentCodeLabel(
            pairs = {
                    @AgentCodePair(code = "WALLET", label = "使用钱包余额进行支付"),
                    @AgentCodePair(code = "ALIPAY", label = "使用支付宝进行支付"),
                    @AgentCodePair(code = "WECHAT_PAY", label = "使用微信支付进行支付"),
                    @AgentCodePair(code = "BANK_CARD", label = "使用银行卡进行支付"),
                    @AgentCodePair(code = "WAIT_PAY", label = "待支付"),
                    @AgentCodePair(code = "CANCELLED", label = "订单已取消")
            }
    )
    private String payType;

    @Schema(description = "支付方式名称")
    private String payTypeName;

    @Schema(description = "订单数量")
    private Long count;

    @Schema(description = "金额合计")
    private BigDecimal amount;
}
