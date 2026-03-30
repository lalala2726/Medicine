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

    @Schema(description = "业务节点模型槽位配置")
    private AgentModelSelectionVo businessNodeModel;

    @Schema(description = "聊天模型槽位配置")
    private AgentModelSelectionVo chatModel;

    @Schema(description = "诊断节点模型槽位配置")
    private AgentModelSelectionVo diagnosisNodeModel;
}
