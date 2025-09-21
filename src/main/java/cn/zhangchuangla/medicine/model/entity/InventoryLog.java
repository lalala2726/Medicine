package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 库存流水表
 */
@TableName(value = "inventory_log")
@Data
public class InventoryLog {

    /**
     * 流水ID
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
     * 变动类型（IN-入库，OUT-出库，SALE-销售，REFUND-退货等）
     */
    private String changeType;

    /**
     * 变动数量（正数入库，负数出库）
     */
    private Integer changeQty;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;
}
