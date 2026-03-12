package cn.zhangchuangla.medicine.model.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理端助手 Agent 配置。
 */
@Data
public class AdminAssistantAgentConfig implements Serializable {

    /**
     * 路由模型槽位配置
     */
    private AgentModelSlotConfig routeModel;

    /**
     * 业务节点普通模型槽位配置
     */
    private AgentModelSlotConfig businessNodeSimpleModel;

    /**
     * 业务节点复杂模型槽位配置
     */
    private AgentModelSlotConfig businessNodeComplexModel;

    /**
     * 聊天界面模型槽位配置
     */
    private AgentModelSlotConfig chatModel;
}
