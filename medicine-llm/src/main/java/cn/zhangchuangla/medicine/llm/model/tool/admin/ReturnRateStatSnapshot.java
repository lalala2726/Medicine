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
@Schema(description = "商品退货率统计")
public class ReturnRateStatSnapshot {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "售出数量")
    private Long soldQuantity;

    @Schema(description = "售后/退货数量")
    private Long returnQuantity;

    @Schema(description = "退货率，0-1")
    private BigDecimal returnRate;
}
