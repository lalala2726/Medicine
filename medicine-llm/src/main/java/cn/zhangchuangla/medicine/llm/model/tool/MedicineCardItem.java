package cn.zhangchuangla.medicine.llm.model.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 药品推荐卡片中的单个药品信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品卡片条目")
public class MedicineCardItem {

    @Schema(description = "药品ID，用于跳转详情页")
    private String id;

    @Schema(description = "药品名称")
    private String name;

    @Schema(description = "药品图片URL")
    private String image;

    @Schema(description = "当前价格")
    private BigDecimal price;

    @Schema(description = "规格描述")
    private String spec;

    @Schema(description = "功效说明")
    private String efficacy;

    @Schema(description = "是否处方药")
    private Boolean prescription;
}
