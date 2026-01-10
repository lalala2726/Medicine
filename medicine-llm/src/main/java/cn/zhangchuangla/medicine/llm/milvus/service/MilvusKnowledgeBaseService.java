package cn.zhangchuangla.medicine.llm.milvus.service;

/**
 * 知识库向量空间管理
 */
public interface MilvusKnowledgeBaseService {

    /**
     * 创建知识库对应的 Milvus 集合
     *
     * @param knowledgeBaseId 知识库 ID
     */
    void createKnowledgeBaseSpace(Integer knowledgeBaseId);

    /**
     * 删除知识库对应的 Milvus 集合
     *
     * @param knowledgeBaseId 知识库 ID
     */
    void dropKnowledgeBaseSpace(Integer knowledgeBaseId);

    /**
     * 构建集合名称，便于后续插入/删除向量
     *
     * @param knowledgeBaseId 知识库 ID
     * @return 集合名
     */
    String buildCollectionName(Integer knowledgeBaseId);
}
