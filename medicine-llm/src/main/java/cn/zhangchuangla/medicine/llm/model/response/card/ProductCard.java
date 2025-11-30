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
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Data
@Schema(description = "商品卡片")
public class ProductCard implements Card{

    @Schema(description = "卡片类型")
    private CardType cardType;

    @Schema(description = "卡片数据")
    private ProductCardPayload payload;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductCardPayload {

        @Schema(description = "卡片标题")
        private String title;

        @Schema(description = "药品列表")
        private List<MedicineCardItem> medicines;
    }

}
