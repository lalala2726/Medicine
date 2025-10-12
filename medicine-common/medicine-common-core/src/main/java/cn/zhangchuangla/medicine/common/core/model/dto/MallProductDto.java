package cn.zhangchuangla.medicine.common.core.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MallProductDto {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品分类ID，关联 mall_category
     */
    private Long categoryId;

    /**
     * 商品单位（件、盒、瓶等）
     */
    private String unit;

    /**
     * 基础售价
     */
    private BigDecimal price;

    /**
     * 销量
     */
    private Long salesVolume;

    /**
     * 独立库存数量（仅当未绑定药品时生效）
     */
    private Integer stock;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态（1-上架，0-下架）
     */
    private Integer status;

    /**
     * 配送方式（快递、自提、同城配送等）
     */
    private String deliveryType;

    /**
     * 运费模板ID，关联 mall_product_shipping
     */
    private Long shippingId;

    /**
     * 库存绑定类型（0-独立库存，1-绑定药品库存）
     */
    private Integer bindType;

    /**
     * 关联药品ID（当 bind_type=1 时生效）
     */
    private Long medicineId;

    /**
     * 关联药品库存批次ID（可选）
     */
    private Long medicineStockId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 所属分类
     */
    private String categoryName;
}
