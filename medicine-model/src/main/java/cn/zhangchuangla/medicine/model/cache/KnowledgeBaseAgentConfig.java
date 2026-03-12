package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 知识库 Agent 配置。
 */
@Data
public class KnowledgeBaseAgentConfig implements Serializable {

    /**
     * 向量维度
     */
    private Integer embeddingDim;

    /**
     * 向量模型槽位配置
     */
    private AgentModelSlotConfig embeddingModel;

    /**
     * Rerank 模型槽位配置
     */
    private AgentModelSlotConfig rerankModel;
}
