package cn.zhangchuangla.medicine.llm.model.response.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
public class MedicineRecommendData {

    /**
     * 卡片标题
     */
    private String title;

    /**
     * 卡片描述
     */
    private String description;

    /**
     * 推荐药品列表
     */
    private List<Medicines> medicines;

    /**
     * 药品信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Medicines {

        /**
         * 商品 ID
         */
        private Long id;

        /**
         * 商品名称
         */
        private String name;

        /**
         * 商品图片
         */
        private String image;

        /**
         * 商品价格
         */
        private BigDecimal price;

        /**
         * 商品规格
         */
        private String spec;

        /**
         * 功效
         */
        private String efficacy;

        /**
         * 是否处方药
         */
        private Boolean prescription;

        /**
         * 购买数量
         */
        private Integer quantity;
    }
}
