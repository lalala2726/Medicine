package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档切片状态更新请求。
 */
@Data
@Schema(description = "更新文档切片状态请求参数")
public class DocumentSliceUpdateRequest {

    @NotNull(message = "切片ID不能为空")
    @Schema(description = "切片ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    private Long chunkId;

    @NotNull(message = "切片状态不能为空")
    @Min(value = 0, message = "切片状态只允许为0或1")
    @Max(value = 1, message = "切片状态只允许为0或1")
    @Schema(description = "切片状态：0启用，1禁用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;
}
