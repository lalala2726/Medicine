package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户端助手 Agent 配置请求对象。
 */
@Data
@Schema(description = "客户端助手Agent配置请求对象")
public class ClientAssistantAgentConfigRequest {

    @Schema(description = "路由模型槽位配置")
    @Valid
    @NotNull(message = "路由模型槽位配置不能为空")
    private AgentModelSelectionRequest routeModel;

    @Schema(description = "聊天模型槽位配置")
    @Valid
    @NotNull(message = "聊天模型槽位配置不能为空")
    private AgentModelSelectionRequest chatModel;

    @Schema(description = "订单模型槽位配置")
    @Valid
    @NotNull(message = "订单模型槽位配置不能为空")
    private AgentModelSelectionRequest orderModel;

    @Schema(description = "商品模型槽位配置")
    @Valid
    @NotNull(message = "商品模型槽位配置不能为空")
    private AgentModelSelectionRequest productModel;

    @Schema(description = "售后模型槽位配置")
    @Valid
    @NotNull(message = "售后模型槽位配置不能为空")
    private AgentModelSelectionRequest afterSaleModel;

    @Schema(description = "问诊安抚模型槽位配置")
    @Valid
    @NotNull(message = "问诊安抚模型槽位配置不能为空")
    private AgentModelSelectionRequest consultationComfortModel;

    @Schema(description = "问诊追问模型槽位配置")
    @Valid
    @NotNull(message = "问诊追问模型槽位配置不能为空")
    private AgentModelSelectionRequest consultationQuestionModel;

    @Schema(description = "问诊最终诊断模型槽位配置")
    @Valid
    @NotNull(message = "问诊最终诊断模型槽位配置不能为空")
    private AgentModelSelectionRequest consultationFinalDiagnosisModel;
}
