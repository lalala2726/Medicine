package cn.zhangchuangla.medicine.common.core.model.request.medicine;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询药品库存请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MedicineStockQueryRequest extends BasePageRequest {

    /**
     * 药品ID
     */
    private Long medicineId;

    /**
     * 药品名称
     */
    private String medicineName;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 仓库位置
     */
    private String warehouse;

    /**
     * 是否过期（null-全部，true-已过期，false-未过期）
     */
    private Boolean isExpired;

    /**
     * 库存状态（low-低库存，normal-正常，high-高库存）
     */
    private String stockStatus;
}
