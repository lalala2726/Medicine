package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端助手 Agent 配置视图对象。
 */
@Data
@Schema(description = "管理端助手Agent配置视图对象")
public class AdminAssistantAgentConfigVo {

    @Schema(description = "管理端节点模型槽位配置")
    private AgentModelSelectionVo adminNodeModel;
}
