package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库 Agent 配置视图对象。
 */
@Data
@Schema(description = "知识库Agent配置视图对象")
public class KnowledgeBaseAgentConfigVo {

    @Schema(description = "向量维度", example = "1024")
    private Integer embeddingDim;

    @Schema(description = "向量模型槽位配置")
    private AgentModelSelectionVo embeddingModel;

    @Schema(description = "Rerank模型槽位配置，可为空表示未配置")
    private AgentModelSelectionVo rerankModel;
}
