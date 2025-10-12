package cn.zhangchuangla.medicine.common.core.model.request.medicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 供应商更新请求对象
 */
@Data
@Schema(description = "供应商更新请求对象")
public class SupplierUpdateRequest {

    /**
     * 供应商ID
     */
    @NotNull(message = "供应商ID不能为空")
    @Schema(description = "供应商ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 供应商名称
     */
    @NotBlank(message = "供应商名称不能为空")
    @Schema(description = "供应商名称", type = "string", example = "华北医药供应商", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    @Schema(description = "联系人", type = "string", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contact;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", type = "string", example = "13800000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /**
     * 供应商地址
     */
    @Schema(description = "供应商地址", type = "string", example = "北京市朝阳区XX路")
    private String address;
}
