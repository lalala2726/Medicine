package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/6
 */
@Data
@Schema(description = "删除文档请求参数")
public class DocumentDeleteRequest {


    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer knowledgeBaseId;

    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long documentId;


}
