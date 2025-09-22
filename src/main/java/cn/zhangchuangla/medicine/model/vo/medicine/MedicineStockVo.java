package cn.zhangchuangla.medicine.model.vo.medicine;

import lombok.Data;

import java.util.Date;

/**
 * 药品库存视图对象
 */
@Data
public class MedicineStockVo {

    /**
     * 库存ID
     */
    private Long id;

    /**
     * 药品ID
     */
    private Long medicineId;

    /**
     * 药品名称
     */
    private String medicineName;

    /**
     * 药品通用名
     */
    private String genericName;

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
     * 是否过期
     */
    private Boolean expired;

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