package cn.zhangchuangla.medicine.common.core.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品SKU规格实体类
 * <p>
 * 用于存储商城商品SKU规格信息的数据库实体，支持商品的
 * 多规格管理，如不同容量、包装、颜色等规格的价格和库存管理。
 *
 * @author Chuang
 * created on 2025/10/4 02:14
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
     * 规格库存
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
     * 排序
     */
    private Integer sort;

    /**
     * 状态（1-启用，0-禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
