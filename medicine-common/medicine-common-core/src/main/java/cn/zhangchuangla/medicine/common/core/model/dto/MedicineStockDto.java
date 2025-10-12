package cn.zhangchuangla.medicine.common.core.model.dto;

import cn.zhangchuangla.medicine.common.core.model.entity.Medicine;
import lombok.Data;

import java.util.Date;

/**
 * 药品库存表
 */
@Data
public class MedicineStockDto {

    /**
     * 库存ID
     */
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

    /**
     * 药品名称
     */
    private Medicine medicine;
}
