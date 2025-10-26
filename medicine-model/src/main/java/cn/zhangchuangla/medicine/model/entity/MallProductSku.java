package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品SKU规格主表（支持药品库存绑定与同步策略）
 */
@TableName(value = "mall_product_sku")
@Data
public class MallProductSku {
    /**
     * SKU主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID，关联 mall_product
     */
    private Long productId;

    /**
     * 规格名称（如：100ml、两件装）
     */
    private String skuName;

    /**
     * 规格价格
     */
    private BigDecimal price;

    /**
     * 规格库存（仅当未绑定药品时生效）
     */
    private Integer stock;

    /**
     * 规格销量
     */
    private Long salesVolume;

    /**
     * 规格图片URL
     */
    private String imageUrl;

    /**
     * 排序值（越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（1-启用，0-禁用）
     */
    private Integer status;

    /**
     * 库存绑定类型（0-独立库存，1-绑定药品库存）
     */
    private Integer bindType;

    /**
     * 绑定药品ID（当 bind_type=1 时生效）
     */
    private Long medicineId;

    /**
     * 绑定药品库存批次ID（可选）
     */
    private Long medicineStockId;

    /**
     * 库存同步策略（1下单扣减，2支付后扣减，3发货后扣减）
     */
    private Integer syncStrategy;

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
     * 是否删除（0-否，1-是）
     */
    private Integer isDelete;
}
