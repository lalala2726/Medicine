package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 药品价格表
 */
@TableName(value = "medicine_price")
@Data
public class MedicinePrice {

    /**
     * 价格ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 药品ID，关联 medicine
     */
    private Long medicineId;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

    /**
     * 促销价
     */
    private BigDecimal promotionPrice;

    /**
     * 价格生效时间
     */
    private Date startTime;

    /**
     * 价格失效时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
