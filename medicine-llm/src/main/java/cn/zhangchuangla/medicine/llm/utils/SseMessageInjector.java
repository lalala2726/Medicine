package cn.zhangchuangla.medicine.llm.utils;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程上下文级的 SSE 消息插入器，便于在任意位置追加提示消息。
 */
@Component
public class SseMessageInjector {

    private static final ThreadLocal<SseStreamBridge.SseSession> SESSION_HOLDER = new ThreadLocal<>();
    private static final AtomicReference<SseStreamBridge.SseSession> GLOBAL_SESSION = new AtomicReference<>();

    public void attach(SseStreamBridge.SseSession session) {
        SESSION_HOLDER.set(session);
        GLOBAL_SESSION.set(session);
    }

    public void clear() {
        SESSION_HOLDER.remove();
        GLOBAL_SESSION.set(null);
    }

    /**
     * 插入一条消息，外部可自定义内容/role 等。
     */
    public void send(ClientChatResponse response) {
        SseStreamBridge.SseSession session = currentSession();
        if (session == null) {
            return;
        }
        if (response.getRole() == null) {
            response.setRole(MessageRole.ASSISTANT);
        }
        session.send(response);
    }

    /**
     * 发送消息；当 asLast=true 时，标记 isFinish=true 并保证在流结束时压轴发送。
     */
    public void send(ClientChatResponse response, boolean asLast) {
        if (response == null) {
            return;
        }
        SseStreamBridge.SseSession session = currentSession();
        if (session == null) {
            return;
        }
        if (response.getRole() == null) {
            response.setRole(MessageRole.ASSISTANT);
        }

        if (asLast) {
            if (response.getIsFinish() == null) {
                response.setIsFinish(true);
            }
            session.sendLast(response);
        } else {
            session.send(response);
        }
    }

    private SseStreamBridge.SseSession currentSession() {
        SseStreamBridge.SseSession session = SESSION_HOLDER.get();
        if (session == null) {
            session = GLOBAL_SESSION.get();
        }
        return session;
    }
}
