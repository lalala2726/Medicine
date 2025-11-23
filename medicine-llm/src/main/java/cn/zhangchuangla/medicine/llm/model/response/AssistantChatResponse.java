package cn.zhangchuangla.medicine.llm.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/24
 */
@Data
@Schema(description = "助手聊天响应")
public class AssistantChatResponse {

    @Schema(description = "助手回复内容")
    private String content;

}
