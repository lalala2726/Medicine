package cn.zhangchuangla.medicine.llm.utils;

import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 将 AI 的 Flux 流封装为 SSE，并提供手动插入消息的能力；测试/外部可自行定时发送。
 */
@Component
public class SseStreamBridge {

    private static final long DEFAULT_TIMEOUT_MS = 30_000L;

    private static void sendSafe(SseEmitter emitter, ChatResponse content) {
        try {
            emitter.send(content);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private static void complete(SseEmitter emitter, AtomicBoolean done) {
        if (done.compareAndSet(false, true)) {
            emitter.complete();
        }
    }

    private static void flushPrev(SseEmitter emitter,
                                  AtomicBoolean done,
                                  AtomicReference<ChatResponse> buffer,
                                  ChatResponse next) {
        if (done.get()) {
            return;
        }
        ChatResponse prev = buffer.getAndSet(next);
        if (prev == null) {
            return;
        }
        if (prev.getIsFinish() == null) {
            prev.setIsFinish(false);
        }
        sendSafe(emitter, prev);
    }

    private static void flushLast(SseEmitter emitter,
                                  AtomicBoolean done,
                                  AtomicReference<ChatResponse> buffer,
                                  AtomicReference<ChatResponse> lastMessage) {
        if (done.get()) {
            return;
        }
        ChatResponse last = lastMessage.getAndSet(null);
        if (last == null) {
            last = buffer.getAndSet(null);
            if (last != null && last.getIsFinish() == null) {
                last.setIsFinish(true);
            }
        } else {
            if (last.getIsFinish() == null) {
                last.setIsFinish(true);
            }
            buffer.set(null);
        }
        if (last != null) {
            sendSafe(emitter, last);
        }
    }

    /**
     * 将 Flux<ChatResponse> 包装为 SSE，支持手动插入消息。
     *
     * @param stream AI 输出流
     * @return 会话包装，包含 emitter 和手动发送能力
     */
    public SseSession bridge(Flux<ChatResponse> stream) {
        return bridge(stream, null);
    }

    public SseSession bridge(Flux<ChatResponse> stream, Runnable onFinish) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<ChatResponse> lastMessage = new AtomicReference<>();
        AtomicReference<ChatResponse> buffer = new AtomicReference<>();

        stream.doOnNext(content -> flushPrev(emitter, done, buffer, content))
                .doOnComplete(() -> {
                    flushLast(emitter, done, buffer, lastMessage);
                    complete(emitter, done);
                    if (onFinish != null) {
                        onFinish.run();
                    }
                })
                .doOnError(ex -> {
                    done.set(true);
                    emitter.completeWithError(ex);
                    if (onFinish != null) {
                        onFinish.run();
                    }
                })
                .subscribe();

        return new SseSession(emitter, done, lastMessage, buffer);
    }

    /**
     * 会话包装，允许随时插入消息。
     */
    public record SseSession(SseEmitter emitter, AtomicBoolean done, AtomicReference<ChatResponse> lastMessage,
                             AtomicReference<ChatResponse> buffer) {

        public void send(ChatResponse content) {
            if (content == null || done.get()) {
                return;
            }
            sendSafe(emitter, content);
        }


        /**
         * 标记一条消息为流结束时的最后推送，底层会在 Flux 完成时发送并设置 isFinish=true（如果未显式指定）。
         */
        public void sendLast(ChatResponse content) {
            if (content == null || done.get()) {
                return;
            }
            lastMessage.set(content);
        }

    }
}
