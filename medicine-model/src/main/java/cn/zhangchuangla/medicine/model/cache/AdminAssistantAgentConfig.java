package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理端助手 Agent 配置。
 */
@Data
public class AdminAssistantAgentConfig implements Serializable {

    /**
     * 管理端节点模型槽位配置。
     */
    private AgentModelSlotConfig adminNodeModel;
}
