package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
@Data
@Schema(description = "客户端聊天响应")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientChatResponse {

    @Schema(description = "消息发送方角色：USER / ASSISTANT / SYSTEM")
    private MessageRole role;

    @Schema(description = "完整文本内容")
    private String content;

    @Schema(description = "消息类型：TEXT / CARD")
    private MessageType type;

    @Schema(description = "卡片消息")
    private List<Card> card;

    @Schema(description = "是否结束")
    private Boolean isFinish;
}
