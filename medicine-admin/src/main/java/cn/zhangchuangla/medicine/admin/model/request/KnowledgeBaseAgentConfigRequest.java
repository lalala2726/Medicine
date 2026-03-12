package cn.zhangchuangla.medicine.admin.model.request;

import cn.zhangchuangla.medicine.common.core.annotation.PowerOfTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识库 Agent 配置请求对象。
 */
@Data
@Schema(description = "知识库Agent配置请求对象")
public class KnowledgeBaseAgentConfigRequest {

    @Schema(description = "向量维度", example = "1024")
    @NotNull(message = "向量维度不能为空")
    @Min(value = 128L, message = "向量维度不能小于128")
    @Max(value = 8192L, message = "向量维度不能大于8192")
    @PowerOfTwo(message = "向量维度必须是2的次方")
    private Integer embeddingDim;

    @Schema(description = "向量模型槽位配置")
    @Valid
    @NotNull(message = "向量模型槽位配置不能为空")
    private AgentModelSelectionRequest embeddingModel;

    @Schema(description = "Rerank模型槽位配置，可为空表示未配置")
    @Valid
    private AgentModelSelectionRequest rerankModel;
}
