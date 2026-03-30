package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户端助手 Agent 配置。
 */
@Data
public class ClientAssistantAgentConfig implements Serializable {

    /**
     * 路由节点模型槽位配置。
     */
    private AgentModelSlotConfig routeModel;

    /**
     * 业务节点模型槽位配置。
     */
    private AgentModelSlotConfig businessNodeModel;

    /**
     * 聊天节点模型槽位配置。
     */
    private AgentModelSlotConfig chatModel;

    /**
     * 诊断节点模型槽位配置。
     */
    private AgentModelSlotConfig diagnosisNodeModel;
}
