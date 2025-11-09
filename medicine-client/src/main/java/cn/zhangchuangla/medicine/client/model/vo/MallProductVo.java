package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/17
 */
@Data
public class MallProductVo {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "商品名称")
    private String name;

    /**
     * 商品单位（件、盒、瓶等）
     */
    @Schema(description = "商品单位（件、盒、瓶等）", example = "件")
    private String unit;

    /**
     * 展示价/兜底价：单规格=唯一SKU价，多规格=最小SKU价；结算以SKU价为准
     */
    @Schema(description = "展示价/兜底价：单规格=唯一SKU价，多规格=最小SKU价；结算以SKU价为准", example = "10.00")
    private BigDecimal price;

    /**
     * 销量
     */
    @Schema(description = "销量", example = "1")
    private Long salesVolume;

    /**
     * 药品说明信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Instruction {

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


}
