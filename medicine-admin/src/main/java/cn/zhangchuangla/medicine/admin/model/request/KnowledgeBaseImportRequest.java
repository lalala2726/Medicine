package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
    @Schema(description = "知识库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long knowledgeBaseId;

    @Schema(description = "导入文件(文件地址)", example = "https://example.com/file.pdf")
    private List<String> fileUrls;
}
