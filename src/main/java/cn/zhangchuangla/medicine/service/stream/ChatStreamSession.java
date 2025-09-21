package cn.zhangchuangla.medicine.service.stream;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import reactor.core.publisher.FluxSink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Helper that wraps a {@link FluxSink} for streaming chat responses via SSE.
 * It offers a couple of convenience methods so callers can push structured
 * events without dealing with Reactor internals.
 */
public class ChatStreamSession {

    private final FluxSink<StreamChatResponse> sink;
    private final String uuid;
    private final AtomicReference<ChatStageEnum> activeStage = new AtomicReference<>();

    public ChatStreamSession(String uuid, FluxSink<StreamChatResponse> sink) {
        this.uuid = uuid;
        this.sink = sink;
    }

    public void send(StreamChatResponse response) {
        if (response == null) {
            return;
        }
        sink.next(response);
    }

    public void send(String uuid, Consumer<StreamChatResponse.StreamChatResponseBuilder> customizer) {
        if (customizer == null) {
            return;
        }
        StreamChatResponse.StreamChatResponseBuilder builder = StreamChatResponse.builder()
                .uuid(uuid)
                .finished(Boolean.FALSE)
                .stage(activeStage.get() == null ? null : activeStage.get().getCode());
        customizer.accept(builder);
        sink.next(builder.build());
    }

    public void send(Consumer<StreamChatResponse.StreamChatResponseBuilder> customizer) {
        send(this.uuid, customizer);
    }

    public void sendStage(ChatStageEnum stage, String content, boolean finished) {
        if (stage == null) {
            return;
        }
        StreamChatResponse response = StreamChatResponse.builder()
                .uuid(uuid)
                .stage(stage.getCode())
                .content(content)
                .finished(finished)
                .build();
        sink.next(response);
    }

    public void sendStage(ChatStageEnum stage, String content, boolean finished, Consumer<StreamChatResponse.StreamChatResponseBuilder> customizer) {
        if (stage == null) {
            return;
        }
        StreamChatResponse.StreamChatResponseBuilder builder = StreamChatResponse.builder()
                .uuid(uuid)
                .stage(stage.getCode())
                .content(content)
                .finished(finished);
        if (customizer != null) {
            customizer.accept(builder);
        }
        sink.next(builder.build());
    }

    public void markStage(ChatStageEnum stage) {
        activeStage.set(stage);
    }

    public Optional<ChatStageEnum> currentStage() {
        return Optional.ofNullable(activeStage.get());
    }

    public void sendStatus(String uuid, String event, Map<String, Object> meta) {
        StreamChatResponse response = StreamChatResponse.builder()
                .uuid(uuid)
                .stage(activeStage.get() == null ? null : activeStage.get().getCode())
                .event(event)
                .meta(meta)
                .finished(Boolean.FALSE)
                .build();
        sink.next(response);
    }

    public void sendChunk(String uuid, String content, int index) {
        sendChunk(uuid, content, index, null);
    }

    public void sendChunk(String uuid, String content, int index, Map<String, Object> extraMeta) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("chunkIndex", index);
        if (extraMeta != null && !extraMeta.isEmpty()) {
            meta.putAll(extraMeta);
        }
        StreamChatResponse response = StreamChatResponse.builder()
                .uuid(uuid)
                .content(content)
                .meta(meta)
                .finished(Boolean.FALSE)
                .stage(activeStage.get() == null ? null : activeStage.get().getCode())
                .build();
        sink.next(response);
    }

    public void complete(String uuid, String messageUuid, Map<String, Object> meta) {
        StreamChatResponse response = StreamChatResponse.builder()
                .uuid(uuid)
                .messageUuid(messageUuid)
                .meta(meta)
                .finished(Boolean.TRUE)
                .content("")
                .stage(ChatStageEnum.COMPLETED.getCode())
                .build();
        sink.next(response);
        sink.complete();
    }

    public void error(Throwable throwable) {
        sink.error(throwable);
    }
}
