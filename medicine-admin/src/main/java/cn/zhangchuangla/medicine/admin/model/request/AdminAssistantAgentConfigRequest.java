package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理端助手 Agent 配置请求对象。
 */
@Data
@Schema(description = "管理端助手Agent配置请求对象")
public class AdminAssistantAgentConfigRequest {

    @Schema(description = "路由模型槽位配置")
    @Valid
    @NotNull(message = "路由模型槽位配置不能为空")
    private AgentModelSelectionRequest routeModel;

    @Schema(description = "业务节点普通模型槽位配置")
    @Valid
    @NotNull(message = "业务节点普通模型槽位配置不能为空")
    private AgentModelSelectionRequest businessNodeSimpleModel;

    @Schema(description = "业务节点复杂模型槽位配置")
    @Valid
    @NotNull(message = "业务节点复杂模型槽位配置不能为空")
    private AgentModelSelectionRequest businessNodeComplexModel;

    @Schema(description = "聊天界面模型槽位配置")
    @Valid
    @NotNull(message = "聊天界面模型槽位配置不能为空")
    private AgentModelSelectionRequest chatModel;
}
