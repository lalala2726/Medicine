package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户端助手 Agent 配置视图对象。
 */
@Data
@Schema(description = "客户端助手Agent配置视图对象")
public class ClientAssistantAgentConfigVo {

    @Schema(description = "路由模型槽位配置")
    private AgentModelSelectionVo routeModel;

    @Schema(description = "聊天模型槽位配置")
    private AgentModelSelectionVo chatModel;

    @Schema(description = "订单模型槽位配置")
    private AgentModelSelectionVo orderModel;

    @Schema(description = "商品模型槽位配置")
    private AgentModelSelectionVo productModel;

    @Schema(description = "售后模型槽位配置")
    private AgentModelSelectionVo afterSaleModel;

    @Schema(description = "问诊安抚模型槽位配置")
    private AgentModelSelectionVo consultationComfortModel;

    @Schema(description = "问诊追问模型槽位配置")
    private AgentModelSelectionVo consultationQuestionModel;

    @Schema(description = "问诊最终诊断模型槽位配置")
    private AgentModelSelectionVo consultationFinalDiagnosisModel;
}
