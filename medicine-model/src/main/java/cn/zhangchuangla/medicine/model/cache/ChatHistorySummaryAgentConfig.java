package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天历史总结 Agent 配置。
 */
@Data
public class ChatHistorySummaryAgentConfig implements Serializable {

    /**
     * 聊天历史总结模型配置
     */
    private AgentModelSlotConfig chatHistorySummaryModel;
}
