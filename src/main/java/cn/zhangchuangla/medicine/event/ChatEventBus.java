package cn.zhangchuangla.medicine.event;

import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 全局聊天事件总线，负责将阶段性响应广播给订阅者。
 */
@Slf4j
@Component
public class ChatEventBus {

    private final ConcurrentMap<String, Sinks.Many<StreamChatResponse>> sinks = new ConcurrentHashMap<>();

    public Flux<StreamChatResponse> register(String channelKey) {
        Sinks.Many<StreamChatResponse> sink = sinks.computeIfAbsent(channelKey,
                key -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux()
                .doFinally(signal -> sinks.remove(channelKey, sink));
    }

    public void publish(String channelKey, StreamChatResponse response) {
        Sinks.Many<StreamChatResponse> sink = sinks.get(channelKey);
        if (sink == null) {
            log.debug("No subscribers for channel {}, dropping event {}", channelKey, response);
            return;
        }
        sink.emitNext(response, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public void complete(String channelKey) {
        Sinks.Many<StreamChatResponse> sink = sinks.remove(channelKey);
        if (sink != null) {
            sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }

    public void error(String channelKey, Throwable throwable) {
        Sinks.Many<StreamChatResponse> sink = sinks.remove(channelKey);
        if (sink != null) {
            sink.emitError(throwable, Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }
}
