package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * Agent 全量配置缓存对象。
 * <p>
 * 该对象作为 Redis 中 agent:config:all 的最终保存结构，供管理端写入、Agent 端读取。
 */
@Data
public class AgentAllConfigCache implements Serializable {

    /**
     * 配置更新时间
     */
    private String updatedAt;

    /**
     * 配置更新人
     */
    private String updatedBy;

    /**
     * 知识库相关 Agent 配置
     */
    private KnowledgeBaseAgentConfig knowledgeBase;

    /**
     * 管理端助手相关 Agent 配置
     */
    private AdminAssistantAgentConfig adminAssistant;

    /**
     * 图片识别相关 Agent 配置
     */
    private ImageRecognitionAgentConfig imageRecognition;

    /**
     * 聊天历史总结相关 Agent 配置
     */
    private ChatHistorySummaryAgentConfig chatHistorySummary;

    /**
     * 聊天标题生成相关 Agent 配置
     */
    private ChatTitleAgentConfig chatTitle;
}
