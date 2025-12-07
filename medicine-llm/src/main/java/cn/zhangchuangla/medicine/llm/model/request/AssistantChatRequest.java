package cn.zhangchuangla.medicine.llm.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/7
 */
@Data
@Schema(description = "助手聊天请求参数")
public class AssistantChatRequest {

    @Schema(description = "用户输入的消息", example = "你好")
    private String message;

    @Schema(description = "上传的文件地址", example = "https://example.com/file.pdf")
    private List<String> fileUrls;
}
