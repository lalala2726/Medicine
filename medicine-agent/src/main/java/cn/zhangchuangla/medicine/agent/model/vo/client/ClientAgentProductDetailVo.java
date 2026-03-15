package cn.zhangchuangla.medicine.agent.model.vo.client;

import cn.zhangchuangla.medicine.agent.annotation.AgentCodeLabel;
import cn.zhangchuangla.medicine.agent.annotation.FieldDescription;
import cn.zhangchuangla.medicine.agent.mapping.AgentCodeLabelRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户端智能体商品详情。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "客户端智能体商品详情")
@FieldDescription(description = "客户端智能体商品详情")
public class ClientAgentProductDetailVo {

    /**
     * 商品ID。
     */
    @Schema(description = "商品ID", example = "1")
    @FieldDescription(description = "商品ID")
    private Long id;

    /**
     * 商品名称。
     */
    @Schema(description = "商品名称", example = "999感冒灵颗粒")
    @FieldDescription(description = "商品名称")
    private String name;

    /**
     * 商品分类ID。
     */
    @Schema(description = "商品分类ID", example = "1")
    @FieldDescription(description = "商品分类ID")
    private Long categoryId;

    /**
     * 商品分类名称。
     */
    @Schema(description = "商品分类名称", example = "感冒药")
    @FieldDescription(description = "商品分类名称")
    private String categoryName;

    /**
     * 商品单位。
     */
    @Schema(description = "商品单位", example = "盒")
    @FieldDescription(description = "商品单位")
    private String unit;

    /**
     * 商品价格。
     */
    @Schema(description = "商品价格", example = "29.90")
    @FieldDescription(description = "商品价格")
    private BigDecimal price;

    /**
     * 商品库存。
     */
    @Schema(description = "库存", example = "100")
    @FieldDescription(description = "库存")
    private Integer stock;

    /**
     * 商品状态编码。
     */
    @Schema(description = "商品状态", example = "1")
    @FieldDescription(description = "商品状态")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_PRODUCT_STATUS)
    private Integer status;

    /**
     * 配送方式编码。
     */
    @Schema(description = "配送方式", example = "2")
    @FieldDescription(description = "配送方式")
    @AgentCodeLabel(dictKey = AgentCodeLabelRegistry.AGENT_PRODUCT_DELIVERY_TYPE)
    private Integer deliveryType;

    /**
     * 销量。
     */
    @Schema(description = "销量", example = "256")
    @FieldDescription(description = "销量")
    private Integer sales;

    /**
     * 商品图片列表。
     */
    @Schema(description = "商品图片列表")
    @FieldDescription(description = "商品图片列表")
    private List<String> images;

    /**
     * 药品说明信息。
     */
    @Schema(description = "药品说明信息")
    @FieldDescription(description = "药品说明信息")
    private DrugDetail drugDetail;

    @Data
    @Schema(description = "药品说明信息")
    @FieldDescription(description = "药品说明信息")
    public static class DrugDetail {

        /**
         * 药品通用名。
         */
        @Schema(description = "药品通用名", example = "复方氨酚烷胺片")
        @FieldDescription(description = "药品通用名")
        private String commonName;

        /**
         * 药品成分。
         */
        @Schema(description = "成分", example = "对乙酰氨基酚、金刚烷胺等")
        @FieldDescription(description = "成分")
        private String composition;

        /**
         * 性状说明。
         */
        @Schema(description = "性状")
        @FieldDescription(description = "性状")
        private String characteristics;

        /**
         * 包装规格。
         */
        @Schema(description = "包装规格")
        @FieldDescription(description = "包装规格")
        private String packaging;

        /**
         * 有效期。
         */
        @Schema(description = "有效期")
        @FieldDescription(description = "有效期")
        private String validityPeriod;

        /**
         * 贮藏条件。
         */
        @Schema(description = "贮藏条件")
        @FieldDescription(description = "贮藏条件")
        private String storageConditions;

        /**
         * 生产单位。
         */
        @Schema(description = "生产单位")
        @FieldDescription(description = "生产单位")
        private String productionUnit;

        /**
         * 批准文号。
         */
        @Schema(description = "批准文号")
        @FieldDescription(description = "批准文号")
        private String approvalNumber;

        /**
         * 执行标准。
         */
        @Schema(description = "执行标准")
        @FieldDescription(description = "执行标准")
        private String executiveStandard;

        /**
         * 产地类型。
         */
        @Schema(description = "产地类型")
        @FieldDescription(description = "产地类型")
        private String originType;

        /**
         * 是否为外用药。
         */
        @Schema(description = "是否外用药", example = "false")
        @FieldDescription(description = "是否外用药")
        private Boolean isOutpatientMedicine;

        /**
         * 温馨提示。
         */
        @Schema(description = "温馨提示")
        @FieldDescription(description = "温馨提示")
        private String warmTips;

        /**
         * 品牌。
         */
        @Schema(description = "品牌")
        @FieldDescription(description = "品牌")
        private String brand;

        /**
         * 是否处方药。
         */
        @Schema(description = "是否处方药", example = "true")
        @FieldDescription(description = "是否处方药")
        private Boolean prescription;

        /**
         * 功能主治。
         */
        @Schema(description = "功能主治")
        @FieldDescription(description = "功能主治")
        private String efficacy;

        /**
         * 用法用量。
         */
        @Schema(description = "用法用量")
        @FieldDescription(description = "用法用量")
        private String usageMethod;

        /**
         * 不良反应。
         */
        @Schema(description = "不良反应")
        @FieldDescription(description = "不良反应")
        private String adverseReactions;

        /**
         * 注意事项。
         */
        @Schema(description = "注意事项")
        @FieldDescription(description = "注意事项")
        private String precautions;

        /**
         * 禁忌信息。
         */
        @Schema(description = "禁忌")
        @FieldDescription(description = "禁忌")
        private String taboo;

        /**
         * 药品说明书全文。
         */
        @Schema(description = "药品说明书全文")
        @FieldDescription(description = "药品说明书全文")
        private String instruction;
    }
}
