package cn.zhangchuangla.medicine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品说明信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicineDetailDto {

    @Schema(description = "品牌", example = "品牌名称")
    private String brand;

    @Schema(description = "功能主治", example = "功能主治描述")
    private String function;

    @Schema(description = "用法用量", example = "用法用量描述")
    private String usage;

    @Schema(description = "不良反应", example = "不良反应描述")
    private String adverseReactions;

    @Schema(description = "注意事项", example = "注意事项描述")
    private String precautions;

    @Schema(description = "禁忌", example = "禁忌描述")
    private String taboo;
}
