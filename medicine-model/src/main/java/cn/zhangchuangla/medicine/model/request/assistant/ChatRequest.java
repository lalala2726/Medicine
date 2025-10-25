package cn.zhangchuangla.medicine.model.request.assistant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聊天请求DTO
 *
 * @author Chuang
 * @since 2025/9/9
 */
@Data
@Schema(description = "聊天请求参数")
public class ChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", example = "你好，请介绍一下自己", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    /**
     * 对话历史记录
     */
    @Schema(description = "对话历史记录", example = "[{\"role\":\"user\",\"content\":\"你好\"},{\"role\":\"assistant\",\"content\":\"你好！很高兴见到你\"}]")
    private List<ChatMessage> conversationHistory;

    /**
     * 是否流式响应
     */
    @NotNull(message = "流式响应标志不能为空")
    @Schema(description = "是否使用流式响应", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean stream;

    /**
     * 提供商名称（可选，不指定则使用当前配置）
     */
    @Schema(description = "提供商名称", example = "OpenAI")
    private String provider;

    /**
     * 模型名称（可选，不指定则使用当前配置）
     */
    @Schema(description = "模型名称", example = "gpt-4")
    private String model;

    /**
     * 温度参数（可选，覆盖默认配置）
     */
    @Schema(description = "模型温度参数", example = "0.7")
    private String temperature;

    /**
     * 最大Token数（可选，覆盖默认配置）
     */
    @Schema(description = "最大Token数", example = "1000")
    private String maxTokens;

    /**
     * 聊天消息
     */
    @Data
    @Schema(description = "聊天消息")
    public static class ChatMessage implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 消息角色
         */
        @NotBlank(message = "消息角色不能为空")
        @Schema(description = "消息角色", example = "user", allowableValues = {"user", "assistant", "system"})
        private String role;

        /**
         * 消息内容
         */
        @NotBlank(message = "消息内容不能为空")
        @Schema(description = "消息内容", example = "你好")
        private String content;
    }
}
