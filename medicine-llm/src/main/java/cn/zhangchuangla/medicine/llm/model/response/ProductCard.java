package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.tool.client.MedicineCardItemTool;
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
public class ProductCard implements Card {

    /**
     * 卡片类型
     */
    private CardType cardType;

    /**
     * 卡片数据
     */
    private ProductCardPayload payload;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductCardPayload {

        /**
         * 卡片标题
         */
        private String title;

        /**
         * 卡片描述，可为空
         */
        private List<MedicineCardItemTool> medicines;
    }

}
