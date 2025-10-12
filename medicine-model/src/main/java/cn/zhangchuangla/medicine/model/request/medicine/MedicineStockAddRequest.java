package cn.zhangchuangla.medicine.model.request.medicine;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Date;

/**
 * 添加药品库存请求
 */
@Data
public class MedicineStockAddRequest {

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expiryDate;

    /**
     * 仓库位置
     */
    @NotBlank(message = "仓库位置不能为空")
    private String warehouse;
}
