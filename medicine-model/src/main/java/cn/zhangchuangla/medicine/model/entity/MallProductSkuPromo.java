package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品SKU优惠规则表（支持满件、折扣、满额、限时活动等）
 */
@TableName(value ="mall_product_sku_promo")
@Data
public class MallProductSkuPromo {
    /**
     * 优惠规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 mall_product_sku.id
     */
    private Long skuId;

    /**
     * 优惠类型（quantity-满数量优惠, discount-折扣, amount-满额减, flash-限时特价）
     */
    private String promoType;

    /**
     * 门槛（件数或金额）
     */
    private Integer threshold;

    /**
     * 优惠额度或折扣比例（满减金额或折扣比）
     */
    private BigDecimal discountValue;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 状态（1-启用，0-停用）
     */
    private Integer status;

    /**
     * 规则描述（例如：买3件9折）
     */
    private String description;

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
}
