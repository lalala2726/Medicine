package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
@Data
@Schema(description = "客户端聊天响应")
public class ClientChatResponse {

    @Schema(description = "消息发送方角色：USER / ASSISTANT / SYSTEM")
    private MessageRole role;

    @Schema(description = "完整文本内容")
    private String content;
}
