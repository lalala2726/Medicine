package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 商城商品图片实体类
 * <p>
 * 用于存储商城商品图片信息的数据库实体，包括商品轮播图、
 * 主图、详情图等。支持多图片管理和排序功能。
 *
 * @author Chuang
 * created on 2025/10/4 02:12
 */
@TableName(value = "mall_product_image")
@Data
public class MallProductImage {
    /**
     * 图片ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID，关联 mall_product
     */
    private Long productId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;
}
