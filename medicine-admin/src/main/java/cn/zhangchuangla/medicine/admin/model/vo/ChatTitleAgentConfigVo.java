package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天标题生成 Agent 配置视图对象。
 */
@Data
@Schema(description = "聊天标题生成Agent配置视图对象")
public class ChatTitleAgentConfigVo {

    @Schema(description = "聊天标题模型配置")
    private AgentModelSelectionVo chatTitleModel;
}
