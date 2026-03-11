package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天历史总结 Agent 配置请求对象。
 */
@Data
@Schema(description = "聊天历史总结Agent配置请求对象")
public class ChatHistorySummaryAgentConfigRequest {

    @Schema(description = "聊天历史总结模型配置")
    @Valid
    @NotNull(message = "聊天历史总结模型配置不能为空")
    private AgentModelSelectionRequest chatHistorySummaryModel;
}
