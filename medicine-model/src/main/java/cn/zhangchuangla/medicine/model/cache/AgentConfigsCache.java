package cn.zhangchuangla.medicine.model.cache;

import java.io.Serializable;

/**
 * Redis 中各业务 Agent 配置分组。
 */
public class AgentConfigsCache implements Serializable {

    /**
     * 知识库相关 Agent 配置。
     */
    private KnowledgeBaseAgentConfig knowledgeBase;

    /**
     * 客户端聊天知识库相关 Agent 配置。
     */
    private KnowledgeBaseAgentConfig clientKnowledgeBase;

    /**
     * 管理端助手相关 Agent 配置。
     */
    private AdminAssistantAgentConfig adminAssistant;

    /**
     * 客户端助手相关 Agent 配置。
     */
    private ClientAssistantAgentConfig clientAssistant;

    /**
     * 图片识别相关 Agent 配置。
     */
    private ImageRecognitionAgentConfig imageRecognition;

    /**
     * 聊天历史总结相关 Agent 配置。
     */
    private ChatHistorySummaryAgentConfig chatHistorySummary;

    /**
     * 聊天标题生成相关 Agent 配置。
     */
    private ChatTitleAgentConfig chatTitle;

    public KnowledgeBaseAgentConfig getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(KnowledgeBaseAgentConfig knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    /**
     * 读取客户端聊天知识库相关 Agent 配置。
     *
     * @return 客户端聊天知识库相关 Agent 配置
     */
    public KnowledgeBaseAgentConfig getClientKnowledgeBase() {
        return clientKnowledgeBase;
    }

    /**
     * 写入客户端聊天知识库相关 Agent 配置。
     *
     * @param clientKnowledgeBase 客户端聊天知识库相关 Agent 配置
     */
    public void setClientKnowledgeBase(KnowledgeBaseAgentConfig clientKnowledgeBase) {
        this.clientKnowledgeBase = clientKnowledgeBase;
    }

    public AdminAssistantAgentConfig getAdminAssistant() {
        return adminAssistant;
    }

    public void setAdminAssistant(AdminAssistantAgentConfig adminAssistant) {
        this.adminAssistant = adminAssistant;
    }

    public ClientAssistantAgentConfig getClientAssistant() {
        return clientAssistant;
    }

    public void setClientAssistant(ClientAssistantAgentConfig clientAssistant) {
        this.clientAssistant = clientAssistant;
    }

    public ImageRecognitionAgentConfig getImageRecognition() {
        return imageRecognition;
    }

    public void setImageRecognition(ImageRecognitionAgentConfig imageRecognition) {
        this.imageRecognition = imageRecognition;
    }

    public ChatHistorySummaryAgentConfig getChatHistorySummary() {
        return chatHistorySummary;
    }

    public void setChatHistorySummary(ChatHistorySummaryAgentConfig chatHistorySummary) {
        this.chatHistorySummary = chatHistorySummary;
    }

    public ChatTitleAgentConfig getChatTitle() {
        return chatTitle;
    }

    public void setChatTitle(ChatTitleAgentConfig chatTitle) {
        this.chatTitle = chatTitle;
    }
}
