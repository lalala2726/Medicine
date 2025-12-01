package cn.zhangchuangla.medicine.llm.utils;

import cn.zhangchuangla.medicine.llm.model.enums.EventType;
import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.enums.MessageType;
import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
import cn.zhangchuangla.medicine.llm.model.response.ToolEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程上下文级的 SSE 消息插入器，便于在任意位置追加提示消息。
 */
@Component
@Slf4j
public class SseMessageInjector {


    private static final Set<EventType> SUPPORTED_EVENT_TYPES =
            EnumSet.of(EventType.TOOL_CALL_START, EventType.TOOL_CALL_END);

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
    public void send(ChatResponse response) {
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
    public void send(ChatResponse response, boolean asLast) {
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

    public void callToolAction(EventType toolAction, String description) {
        if (!SUPPORTED_EVENT_TYPES.contains(toolAction)) {
            log.warn("请将事件类型修改为工具类型!：{}", toolAction);
        }

        if (description == null || description.isBlank()) {
            log.warn("工具描述不能为空");
        }

        ToolEvent event = ToolEvent.builder()
                .eventType(toolAction)
                .description(description)
                .build();

        ChatResponse response = ChatResponse.builder()
                .type(MessageType.EVENT)
                .eventData(event)
                .build();

        SseStreamBridge.SseSession session = currentSession();
        if (session == null) {
            log.error("未获取到 SSE 会话，工具事件发送失败，toolAction={}, description={}", toolAction, description);
            throw new IllegalStateException("SSE 会话未建立，无法发送工具事件");
        }
        response.setRole(MessageRole.ASSISTANT);
        session.send(response);
    }
}
