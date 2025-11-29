package cn.zhangchuangla.medicine.llm.utils;

import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将 AI 的 Flux 流封装为 SSE，并提供手动插入消息的能力；测试/外部可自行定时发送。
 */
@Component
public class SseStreamBridge {

    private static final long DEFAULT_TIMEOUT_MS = 30_000L;

    /**
     * 将 Flux<ClientChatResponse> 包装为 SSE，支持手动插入消息。
     *
     * @param stream AI 输出流
     * @return 会话包装，包含 emitter 和手动发送能力
     */
    public SseSession bridge(Flux<ClientChatResponse> stream) {
        return bridge(stream, null);
    }

    public SseSession bridge(Flux<ClientChatResponse> stream, Runnable onFinish) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        AtomicBoolean done = new AtomicBoolean(false);

        stream.doOnNext(content -> sendSafe(emitter, content))
                .doOnComplete(() -> {
                    done.set(true);
                    emitter.complete();
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

        return new SseSession(emitter, done);
    }

    private void sendSafe(SseEmitter emitter, ClientChatResponse content) {
        try {
            emitter.send(content);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 会话包装，允许随时插入消息。
     */
    public static class SseSession {
        private final SseEmitter emitter;
        private final AtomicBoolean done;

        SseSession(SseEmitter emitter, AtomicBoolean done) {
            this.emitter = emitter;
            this.done = done;
        }

        public SseEmitter emitter() {
            return emitter;
        }

        public void send(ClientChatResponse content) {
            if (done.get()) {
                return;
            }
            try {
                emitter.send(content);
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
