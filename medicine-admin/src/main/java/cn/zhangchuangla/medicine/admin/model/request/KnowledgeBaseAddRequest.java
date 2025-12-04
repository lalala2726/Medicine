package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库表，存储所有知识库的元数据
 */
@Data
@Schema(description = "添加知识库请求对象")
public class KnowledgeBaseAddRequest {

    @Schema(description = "知识库名称", example = "知识库名称")
    @NotBlank(message = "知识库名称不能为空")
    private String name;

    @Schema(description = "知识库描述", example = "知识库描述")
    private String description;

    @Schema(description = "封面", example = "https://example.com/cover.jpg")
    private String cover;

}
