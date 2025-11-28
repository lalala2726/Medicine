package cn.zhangchuangla.medicine.common.rabbitmq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 供搜索索引使用的商品基础数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIndexPayload {

    /**
     * 药品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 商品状态
     */
    private Integer status;

    /**
     * 药品品牌
     */
    private String brand;

    /**
     * 药品通用名
     */
    private String commonName;

    /**
     * 药品功效
     */
    private String efficacy;

    /**
     * 提醒信息
     */
    private String warmTips;

    /**
     * 使用说明
     */
    private String instruction;

    /**
     * 封面图片
     */
    private String coverImage;
}
