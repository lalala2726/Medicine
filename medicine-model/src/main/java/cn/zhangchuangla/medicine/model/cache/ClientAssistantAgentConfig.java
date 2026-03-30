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
     * 聊天节点模型槽位配置。
     */
    private AgentModelSlotConfig chatModel;

    /**
     * 订单节点模型槽位配置。
     */
    private AgentModelSlotConfig orderModel;

    /**
     * 商品节点模型槽位配置。
     */
    private AgentModelSlotConfig productModel;

    /**
     * 售后节点模型槽位配置。
     */
    private AgentModelSlotConfig afterSaleModel;

    /**
     * 问诊安抚节点模型槽位配置。
     */
    private AgentModelSlotConfig consultationComfortModel;

    /**
     * 问诊追问节点模型槽位配置。
     */
    private AgentModelSlotConfig consultationQuestionModel;

    /**
     * 问诊最终诊断节点模型槽位配置。
     */
    private AgentModelSlotConfig consultationFinalDiagnosisModel;
}
