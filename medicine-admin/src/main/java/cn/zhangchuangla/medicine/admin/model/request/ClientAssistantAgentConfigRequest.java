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

    @Schema(description = "业务节点模型槽位配置")
    @Valid
    @NotNull(message = "业务节点模型槽位配置不能为空")
    private AgentModelSelectionRequest businessNodeModel;

    @Schema(description = "聊天模型槽位配置")
    @Valid
    @NotNull(message = "聊天模型槽位配置不能为空")
    private AgentModelSelectionRequest chatModel;

    @Schema(description = "诊断节点模型槽位配置")
    @Valid
    @NotNull(message = "诊断节点模型槽位配置不能为空")
    private AgentModelSelectionRequest diagnosisNodeModel;
}
