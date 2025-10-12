package cn.zhangchuangla.medicine.model.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 聊天响应VO
 *
 * @author Chuang
 * @since 2025/9/9
 */
@Data
@Schema(description = "聊天响应结果")
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应ID
     */
    @Schema(description = "响应唯一标识", example = "chat_123456789")
    private String responseId;

    /**
     * 提供商名称
     */
    @Schema(description = "使用的提供商", example = "OpenAI")
    private String provider;

    /**
     * 模型名称
     */
    @Schema(description = "使用的模型", example = "gpt-4")
    private String model;

    /**
     * 回复内容
     */
    @Schema(description = "AI回复内容", example = "你好！我是一个AI助手，很高兴为您服务。")
    private String content;

    /**
     * 使用Token数
     */
    @Schema(description = "消耗的Token数量", example = "150")
    private Integer totalTokens;

    /**
     * 响应时间（毫秒）
     */
    @Schema(description = "响应时间（毫秒）", example = "1200")
    private Long responseTime;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "")
    private String error;

    /**
     * 是否成功
     */
    @Schema(description = "请求是否成功", example = "true")
    private Boolean success;

    /**
     * 创建响应构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 响应构建器
     */
    public static class Builder {
        private ChatResponse response;

        public Builder() {
            this.response = new ChatResponse();
        }

        public Builder responseId(String responseId) {
            response.setResponseId(responseId);
            return this;
        }

        public Builder provider(String provider) {
            response.setProvider(provider);
            return this;
        }

        public Builder model(String model) {
            response.setModel(model);
            return this;
        }

        public Builder content(String content) {
            response.setContent(content);
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            response.setTotalTokens(totalTokens);
            return this;
        }

        public Builder responseTime(Long responseTime) {
            response.setResponseTime(responseTime);
            return this;
        }

        public Builder error(String error) {
            response.setError(error);
            return this;
        }

        public Builder success(Boolean success) {
            response.setSuccess(success);
            return this;
        }

        public ChatResponse build() {
            return response;
        }
    }
}
