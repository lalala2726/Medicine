package cn.zhangchuangla.medicine.agent.model.vo.client;

import cn.zhangchuangla.medicine.agent.annotation.FieldDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 客户端智能体商品购买卡片。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "客户端智能体商品购买卡片")
@FieldDescription(description = "客户端智能体商品购买卡片")
public class ClientAgentProductPurchaseCardsVo {

    /**
     * 整体价格。
     */
    @Schema(description = "整体价格", example = "36.70")
    @FieldDescription(description = "整体价格")
    private String totalPrice;

    /**
     * 商品列表。
     */
    @Schema(description = "商品列表")
    @FieldDescription(description = "商品列表")
    private List<ClientAgentProductPurchaseItemVo> items;

    /**
     * 商品购买卡片单项。
     */
    @Data
    @Schema(description = "商品购买卡片单项")
    @FieldDescription(description = "商品购买卡片单项")
    public static class ClientAgentProductPurchaseItemVo {

        /**
         * 商品ID。
         */
        @Schema(description = "商品ID", example = "101")
        @FieldDescription(description = "商品ID")
        private String id;

        /**
         * 商品名称。
         */
        @Schema(description = "商品名称", example = "布洛芬缓释胶囊")
        @FieldDescription(description = "商品名称")
        private String name;

        /**
         * 商品主图。
         */
        @Schema(description = "商品主图", example = "https://example.com/images/101.png")
        @FieldDescription(description = "商品主图")
        private String image;

        /**
         * 商品销售价。
         */
        @Schema(description = "商品销售价", example = "16.80")
        @FieldDescription(description = "商品销售价")
        private String price;

        /**
         * 规格。
         */
        @Schema(description = "规格", example = "24粒/盒")
        @FieldDescription(description = "规格")
        private String spec;

        /**
         * 功效/适应症。
         */
        @Schema(description = "功效/适应症", example = "缓解发热、头痛")
        @FieldDescription(description = "功效/适应症")
        private String efficacy;

        /**
         * 是否处方药。
         */
        @Schema(description = "是否处方药", example = "false")
        @FieldDescription(description = "是否处方药")
        private Boolean prescription;

        /**
         * 库存。
         */
        @Schema(description = "库存", example = "56")
        @FieldDescription(description = "库存")
        private Integer stock;
    }
}
