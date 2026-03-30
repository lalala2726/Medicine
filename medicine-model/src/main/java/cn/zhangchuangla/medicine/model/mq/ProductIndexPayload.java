package cn.zhangchuangla.medicine.model.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供搜索索引使用的商品基础数据。
 *
 * @author Chuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIndexPayload {

    /**
     * 商品ID。
     */
    private Long id;

    /**
     * 商品名称。
     */
    private String name;

    /**
     * 分类名称。
     */
    private String categoryName;

    /**
     * 分类ID。
     */
    private Long categoryId;

    /**
     * 价格。
     */
    private BigDecimal price;

    /**
     * 商品销量。
     */
    private Integer sales;

    /**
     * 商品状态。
     */
    private Integer status;

    /**
     * 药品品牌。
     */
    private String brand;

    /**
     * 药品通用名。
     */
    private String commonName;

    /**
     * 药品功效。
     */
    private String efficacy;

    /**
     * 全量标签ID列表。
     */
    private List<Long> tagIds;

    /**
     * 聚合后的标签名称列表。
     */
    private List<String> tagNames;

    /**
     * 标签类型绑定列表，格式为 typeCode:tagId。
     */
    private List<String> tagTypeBindings;

    /**
     * 提醒信息。
     */
    private String warmTips;

    /**
     * 使用说明。
     */
    private String instruction;

    /**
     * 封面图片。
     */
    private String coverImage;

    /**
     * 是否处方药。
     */
    private Boolean prescription;
}
