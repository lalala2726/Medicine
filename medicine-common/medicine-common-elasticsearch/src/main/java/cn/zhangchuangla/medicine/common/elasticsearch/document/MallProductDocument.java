package cn.zhangchuangla.medicine.common.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

/**
 * 商品搜索索引文档。
 *
 * <p>用于将商城商品和药品信息同步到 Elasticsearch，便于名称/品牌/功效等多字段检索。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = MallProductDocument.INDEX_NAME)
public class MallProductDocument {

    public static final String INDEX_NAME = "mall_product";

    @Id
    private Long id;

    /**
     * 商品名称，提供全文检索和前缀匹配（keyword 子字段用于前缀/模糊）。
     */
    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String name;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer status;

    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String brand;

    /**
     * 药品通用名。
     */
    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String commonName;

    /**
     * 功效/主治。
     */
    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String efficacy;

    /**
     * 主要成分。
     */
    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String composition;

    /**
     * 用法用量。
     */
    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String usageMethod;

    /**
     * 其他提示信息。
     */
    @Field(type = FieldType.Text)
    private String warmTips;

    /**
     * 说明书全文，作为长文本检索补充。
     */
    @Field(type = FieldType.Text)
    private String instruction;

    /**
     * 商品主图。
     */
    @Field(type = FieldType.Keyword, index = false)
    private String coverImage;

    /**
     * 所有图片地址，便于前端快速展示。
     */
    @Field(type = FieldType.Keyword, index = false)
    private List<String> imageUrls;
}
