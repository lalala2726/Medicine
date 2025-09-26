package cn.zhangchuangla.medicine.model.vo.medicine;

import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "库存ID", type = "long", example = "1")
    private Long id;

    /**
     * 药品ID，关联 medicine
     */
    @Schema(description = "药品ID，关联 medicine", type = "long", example = "1")
    private Long medicineId;

    /**
     * 药品名称
     */
    @Schema(description = "药品名称", type = "string", example = "药品名称")
    private String medicineName;

    /**
     * 批次号
     */
    @Schema(description = "批次号", type = "string", example = "批次号")
    private String batchNo;

    /**
     * 库存数量
     */
    @Schema(description = "库存数量", type = "integer", example = "1")
    private Integer quantity;

    /**
     * 有效期
     */
    @Schema(description = "有效期", type = "date", example = "2025-09-22")
    @JSONField(format = "yyyy-MM-dd")
    private Date expiryDate;

    /**
     * 仓库位置
     */
    @Schema(description = "仓库位置", type = "string", example = "仓库位置")
    private String warehouse;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "date", example = "2025-09-22")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", type = "date", example = "2025-09-22")
    private Date updateTime;
}
