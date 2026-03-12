package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天标题生成 Agent 配置请求对象。
 */
@Data
@Schema(description = "聊天标题生成Agent配置请求对象")
public class ChatTitleAgentConfigRequest {

    @Schema(description = "聊天标题模型配置")
    @Valid
    @NotNull(message = "聊天标题模型配置不能为空")
    private AgentModelSelectionRequest chatTitleModel;
}
