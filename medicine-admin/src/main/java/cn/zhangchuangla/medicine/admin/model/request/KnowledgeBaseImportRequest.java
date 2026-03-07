package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/4
 */
@Data
@Schema(description = "知识库导入请求参数")
public class KnowledgeBaseImportRequest {

    @NotNull(message = "知识库ID不能为空")
    @Min(value = 1, message = "知识库ID必须大于0")
    @Schema(description = "知识库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long knowledgeBaseId;

    //todo 切片策略定义为枚举，并且根据枚举下面的属性进行校验

    @NotEmpty(message = "导入文件不能为空")
    @Schema(description = "导入文件列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull(message = "文件详情不能为空") @Valid FileDetail> fileDetails;

    @NotBlank(message = "切片策略不能为空")
    @Schema(description = "切片策略", requiredMode = Schema.RequiredMode.REQUIRED, example = "character")
    private String chunkStrategy;

    @Min(value = 100, message = "切片大小必须大于0")
    @Schema(description = "切片大小", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    private Integer chunkSize;

    @Min(value = 100, message = "token大小必须大于100")
    @Schema(description = "token大小", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer tokenSize;

    @Schema(description = "文件详情")
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class FileDetail {

        /**
         * 文件名
         */
        @NotBlank(message = "文件名字不能为空")
        @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "file.pdf")
        private String fileName;

        /**
         * 文件地址
         */
        @NotBlank(message = "文件地址不能为空")
        @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/file.pdf")
        private String fileUrl;
    }
}
