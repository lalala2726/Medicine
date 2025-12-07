package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档切片更新请求。
 */
@Data
@Schema(description = "更新文档切片请求参数")
public class DocumentSliceUpdateRequest {

    @NotNull(message = "切片ID不能为空")
    @Schema(description = "切片ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    private Long chunkId;

    @NotBlank(message = "切片内容不能为空")
    @Schema(description = "新的切片内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "更新后的切片文本")
    private String content;
}

