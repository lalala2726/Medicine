package cn.zhangchuangla.medicine.model.vo.llm.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 简化聊天响应：仅返回会话UUID、消息UUID和当前回复内容。
 */
@Data
@Schema(description = "聊天响应（单条）")
public class ChatResponse {

    @Schema(description = "会话UUID")
    private String uuid;

    @Schema(description = "消息UUID")
    private String messageUuid;

    @Schema(description = "AI回复内容")
    private String content;
}
