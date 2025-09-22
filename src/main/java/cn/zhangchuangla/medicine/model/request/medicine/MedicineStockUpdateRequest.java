package cn.zhangchuangla.medicine.model.request.medicine;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

/**
 * 更新药品库存请求
 */
@Data
public class MedicineStockUpdateRequest {

    /**
     * 库存ID
     */
    @NotNull(message = "库存ID不能为空")
    private Long id;

    /**
     * 药品ID
     */
    @NotNull(message = "药品ID不能为空")
    private Long medicineId;

    /**
     * 批次号
     */
    @NotBlank(message = "批次号不能为空")
    private String batchNo;

    /**
     * 库存数量
     */
    @NotNull(message = "库存数量不能为空")
    @Positive(message = "库存数量必须大于0")
    private Integer quantity;

    /**
     * 有效期
     */
    @NotNull(message = "有效期不能为空")
    private Date expiryDate;

    /**
     * 仓库位置
     */
    @NotBlank(message = "仓库位置不能为空")
    private String warehouse;
}