package cn.zhangchuangla.medicine.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品运费配置实体类
 * <p>
 * 用于存储商城商品运费模板信息的数据库实体，支持包邮、
 * 固定运费等多种运费配置模式。可被多个商品共享使用。
 *
 * @author Chuang
 * created on 2025/10/4 02:13
 */
@TableName(value = "mall_product_shipping")
@Data
public class MallProductShipping {

    /**
     * 运费模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 运费类型（free-包邮，fixed-固定）
     */
    private String type;

    /**
     * 运费价格（type=fixed时生效）
     */
    private BigDecimal price;

    /**
     * 模板描述
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
}
