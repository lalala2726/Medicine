package cn.zhangchuangla.medicine.llm.model.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付方式分布")
public class PaymentDistributionSnapshot {

    @Schema(description = "支付方式编码")
    private String payType;

    @Schema(description = "支付方式名称")
    private String payTypeName;

    @Schema(description = "订单数量")
    private Long count;

    @Schema(description = "金额合计")
    private BigDecimal amount;
}
