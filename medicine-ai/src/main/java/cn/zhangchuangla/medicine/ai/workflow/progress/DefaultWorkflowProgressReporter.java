package cn.zhangchuangla.medicine.ai.workflow.progress;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import org.springframework.util.StringUtils;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认进度报告器，将进度更新序列化为
 * {@link StreamChatResponse} SSE 负载数据。
 */
public class DefaultWorkflowProgressReporter implements WorkflowProgressReporter {

    private final String conversationUuid;
    private final FluxSink<StreamChatResponse> sink;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public DefaultWorkflowProgressReporter(String conversationUuid, FluxSink<StreamChatResponse> sink) {
        this.conversationUuid = conversationUuid;
        this.sink = sink;
        this.sink.onCancel(() -> cancelled.set(true));
        this.sink.onDispose(() -> cancelled.set(true));
    }

    @Override
    public void publishStage(ChatStageEnum stage, String message, boolean finished) {
        if (isCancelled()) {
            return;
        }
        StreamChatResponse payload = StreamChatResponse.builder()
                .uuid(conversationUuid)
                .stage(stage != null ? stage.getCode() : null)
                .stageMessage(message)
                .finished(finished)
                .build();
        emit(payload);
    }

    @Override
    public void publishToolInvoke(String toolName, String message) {
        if (isCancelled()) {
            return;
        }
        StreamChatResponse payload = StreamChatResponse.builder()
                .uuid(conversationUuid)
                .stage(ChatStageEnum.TOOL_INVOKE.getCode())
                .stageMessage(message)
                .toolName(toolName)
                .finished(Boolean.FALSE)
                .build();
        emit(payload);
    }

    @Override
    public void publishToolResult(String toolName, String result) {
        if (isCancelled()) {
            return;
        }
        StreamChatResponse payload = StreamChatResponse.builder()
                .uuid(conversationUuid)
                .stage(ChatStageEnum.TOOL_RESULT.getCode())
                .toolName(toolName)
                .toolMessage(result)
                .finished(Boolean.FALSE)
                .build();
        emit(payload);
    }

    @Override
    public void publishResponseChunk(String content) {
        if (isCancelled()) {
            return;
        }
        if (!StringUtils.hasText(content)) {
            return;
        }
        StreamChatResponse payload = StreamChatResponse.builder()
                .uuid(conversationUuid)
                .stage(ChatStageEnum.RESPONSE_STREAM.getCode())
                .content(content)
                .finished(Boolean.FALSE)
                .build();
        emit(payload);
    }

    @Override
    public void publishResponseCompleted(String messageUuid, ChatStageEnum finalStage) {
        if (isCancelled()) {
            return;
        }
        ChatStageEnum stage = finalStage != null ? finalStage : ChatStageEnum.COMPLETED;
        StreamChatResponse payload = StreamChatResponse.builder()
                .uuid(conversationUuid)
                .messageUuid(messageUuid)
                .stage(stage.getCode())
                .stageMessage(stage.getDescription())
                .finished(Boolean.TRUE)
                .build();
        emit(payload);
    }

    private void emit(StreamChatResponse payload) {
        try {
            sink.next(payload);
        } catch (Exception ignored) {
            cancelled.set(true);
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }
}
