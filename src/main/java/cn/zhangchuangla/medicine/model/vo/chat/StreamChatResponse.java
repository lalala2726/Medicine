package cn.zhangchuangla.medicine.model.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 流式聊天响应块
 *
 * @author Chuang
 * @since 2025/9/9
 */
@Data
@Schema(description = "流式聊天响应块")
public class StreamChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 使用Token数
     */
    @Schema(description = "消耗的Token数量", example = "150")
    private Integer totalTokens;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "")
    private String error;

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
        private StreamChatResponse response;

        public Builder() {
            this.response = new StreamChatResponse();
        }

        public Builder responseId(String responseId) {
            response.setResponseId(responseId);
            return this;
        }

        public Builder content(String content) {
            response.setContent(content);
            return this;
        }

        public Builder finished(Boolean finished) {
            response.setFinished(finished);
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

        public Builder totalTokens(Integer totalTokens) {
            response.setTotalTokens(totalTokens);
            return this;
        }

        public Builder error(String error) {
            response.setError(error);
            return this;
        }

        public StreamChatResponse build() {
            return response;
        }
    }
}
