package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品运费配置表
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
