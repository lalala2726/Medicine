package cn.zhangchuangla.medicine.model.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流式聊天响应块
 *
 * @author Chuang
 * @since 2025/9/9
 */
@Data
@Schema(description = "流式聊天响应块")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StreamChatResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话UUID
     */
    @Schema(description = "会话UUID")
    private String uuid;

    /**
     * 消息UUID（仅在结束包返回）
     */
    @Schema(description = "消息UUID（完成时返回）")
    private String messageUuid;

    /**
     * 响应ID
     */
    @Schema(description = "响应唯一标识", example = "chat_123456789")
    private String responseId;

    /**
     * 内容块
     */
    @Schema(description = "流式内容块", example = "你好")
    private String content;

    /**
     * 是否完成
     */
    @Schema(description = "是否完成响应", example = "false")
    private Boolean finished;

}
