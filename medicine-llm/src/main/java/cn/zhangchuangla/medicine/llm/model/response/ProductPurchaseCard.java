package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.tool.client.MedicineCardItem;
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

    /**
     * 卡片类型
     */
    private CardType cardType;

    /**
     * 载荷
     */
    private ProductPurchaseCardPayload payload;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductPurchaseCardPayload {

        /**
         * 标题
         */
        private String title;

        /**
         * 描述
         */
        private String description;

        /**
         * 药品列表
         */
        private List<MedicineCardItem> medicines;
    }
}
