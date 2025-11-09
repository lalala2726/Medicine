package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 药品详细信息表
 */
@TableName(value = "medicine_detail")
@Data
public class MedicineDetail {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联商城商品ID（mall_product.id）
     */
    private Long productId;

    /**
     * 品牌名称
     */
    private String brand;

    /**
     * 功能主治
     */
    private String function;

    /**
     * 用法用量
     */
    private String usage;

    /**
     * 不良反应
     */
    private String adverseReactions;

    /**
     * 注意事项
     */
    private String precautions;

    /**
     * 禁忌
     */
    private String taboo;

    /**
     * 药品说明书全文（可选）
     */
    private String instruction;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
