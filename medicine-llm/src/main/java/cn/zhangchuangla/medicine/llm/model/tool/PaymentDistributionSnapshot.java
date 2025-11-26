package cn.zhangchuangla.medicine.llm.model.tool;

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
