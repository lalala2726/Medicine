package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 商城商品详情实体类
 * <p>
 * 用于存储商城商品详情信息的数据库实体，包括商品描述、
 * 功能介绍、使用说明等详细内容。支持HTML和Markdown格式。
 *
 * @author Chuang
 * created on 2025/10/4 02:11
 */
@TableName(value = "mall_product_detail")
@Data
public class MallProductDetail {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID，关联 mall_product
     */
    private Long productId;

    /**
     * 商品详情HTML或Markdown内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
