package cn.zhangchuangla.medicine.llm.model.response.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 药品推荐卡片载荷。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品推荐卡片数据")
public class MedicineRecommendPayload implements CardPayload {

    @Schema(description = "卡片标题")
    private String title;

    @Schema(description = "卡片描述，可为空")
    private String description;

    @Schema(description = "药品列表")
    private List<MedicineCardItem> medicines;
}
