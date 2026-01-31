package cn.zhangchuangla.medicine.agent.service;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
public interface KnowledgeBaseService {

    /**
     * 创建知识库
     *
     * @param knowledgeBaseName        知识库名称
     * @param knowledgeBaseDescription 知识库描述
     * @param embedding_dim            嵌入维度
     */
    void createKnowledgeBase(String knowledgeBaseName, String knowledgeBaseDescription, Integer embedding_dim);

    /**
     * 删除知识库
     *
     * @param knowledgeBaseName 知识库名称
     */
    void deleteKnowledgeBase(String knowledgeBaseName);


}
