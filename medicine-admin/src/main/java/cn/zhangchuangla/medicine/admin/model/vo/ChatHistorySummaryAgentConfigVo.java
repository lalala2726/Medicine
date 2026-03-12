package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天历史总结 Agent 配置视图对象。
 */
@Data
@Schema(description = "聊天历史总结Agent配置视图对象")
public class ChatHistorySummaryAgentConfigVo {

    @Schema(description = "聊天历史总结模型配置")
    private AgentModelSelectionVo chatHistorySummaryModel;
}
