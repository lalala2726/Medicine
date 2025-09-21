package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 药品库存表
 */
@TableName(value ="medicine_stock")
@Data
public class MedicineStock {

    /**
     * 库存ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 药品ID，关联 medicine
     */
    private Long medicineId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 库存数量
     */
    private Integer quantity;

    /**
     * 有效期
     */
    private Date expiryDate;

    /**
     * 仓库位置
     */
    private String warehouse;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
