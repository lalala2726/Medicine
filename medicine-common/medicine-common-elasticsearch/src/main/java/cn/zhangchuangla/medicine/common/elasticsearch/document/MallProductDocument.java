package cn.zhangchuangla.medicine.common.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.math.BigDecimal;

/**
 * ES 商品索引
 * 支持：
 * - IK + 拼音全文搜索
 * - Completion Suggest（自动补全）
 * - 品牌/名称/通用名 三路补全
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = MallProductDocument.INDEX_NAME, createIndex = true)
@Setting(settingPath = "/elasticsearch/mall_product-settings.json")
public class MallProductDocument {

    public static final String INDEX_NAME = "mall_product";

    @Id
    private Long id;

    /**
     * 商品名称（text + keyword + 拼音搜索）
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_pinyin_index", searchAnalyzer = "ik_pinyin_search"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String name;

    /**
     * 分类名
     */
    @Field(type = FieldType.Keyword)
    private String categoryName;

    /**
     * 是否处方药
     */
    @Field(type = FieldType.Boolean)
    private Boolean prescription;

    /**
     * 商品价格
     */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    /**
     * 商品销量
     */
    @Field(type = FieldType.Integer)
    private Integer sales;

    /**
     * 状态
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 品牌（text + 拼音搜索）
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_pinyin_index", searchAnalyzer = "ik_pinyin_search"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String brand;

    /**
     * 通用名（如“对乙酰氨基酚”）
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_pinyin_index", searchAnalyzer = "ik_pinyin_search"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword)
    )
    private String commonName;

    /**
     * 商品名称补全（拼音+中文）
     */
    @CompletionField(analyzer = "ik_pinyin_index", searchAnalyzer = "ik_pinyin_search", maxInputLength = 100)
    private Completion nameSuggest;

    /**
     * 通用名补全
     */
    @CompletionField(analyzer = "ik_smart", searchAnalyzer = "ik_smart", maxInputLength = 100)
    private Completion commonNameSuggest;

    /**
     * 品牌补全
     */
    @CompletionField(analyzer = "ik_smart", searchAnalyzer = "ik_smart", maxInputLength = 100)
    private Completion brandSuggest;

    /**
     * 功效/主治
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String efficacy;

    /**
     * 主图
     */
    @Field(type = FieldType.Keyword, index = false)
    private String coverImage;

    /**
     * 药品说明书
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String instruction;
}
