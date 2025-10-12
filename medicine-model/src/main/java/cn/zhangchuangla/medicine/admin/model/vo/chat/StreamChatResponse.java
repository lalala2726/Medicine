package cn.zhangchuangla.medicine.admin.model.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流式聊天响应块
 * <p>
 * 该响应对象支持同时承载工作流阶段、工具调用信息以及内容分片，便于前端实时展示进度。
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
     * 流式阶段代码
     */
    @Schema(description = "工作流执行阶段编码")
    private String stage;

    /**
     * 阶段说明
     */
    @Schema(description = "阶段说明")
    private String stageMessage;

    /**
     * 工具名称（如果当前阶段为工具调用）
     */
    @Schema(description = "工具名称")
    private String toolName;

    /**
     * 工具输出或提示信息
     */
    @Schema(description = "工具输出或提示信息")
    private String toolMessage;

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
