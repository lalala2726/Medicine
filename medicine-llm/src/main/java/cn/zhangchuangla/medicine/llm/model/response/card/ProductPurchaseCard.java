package cn.zhangchuangla.medicine.llm.model.response.card;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.tool.MedicineCardItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 药品推荐卡片，包装卡片类型与载荷。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPurchaseCard implements Card {

    private CardType cardType;

    private ProductPurchaseCardPayload payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductPurchaseCardPayload {

        @Schema(description = "卡片标题")
        private String title;

        @Schema(description = "卡片描述，可为空")
        private String description;

        @Schema(description = "药品列表")
        private List<MedicineCardItem> medicines;
    }
}
