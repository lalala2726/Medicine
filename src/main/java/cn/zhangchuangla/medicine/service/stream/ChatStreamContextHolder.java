package cn.zhangchuangla.medicine.service.stream;

import cn.zhangchuangla.medicine.enums.ChatStageEnum;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;

import java.util.function.Consumer;

/**
 * Thread-local holder that exposes the active {@link ChatStreamSession} so
 * downstream collaborators can emit streaming responses without threading the
 * session reference through every method signature.
 */
public final class ChatStreamContextHolder {

    private static final ThreadLocal<ChatStreamSession> HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<ChatStageEnum> STAGE_HOLDER = new ThreadLocal<>();

    private ChatStreamContextHolder() {
    }

    public static void set(ChatStreamSession session) {
        if (session == null) {
            return;
        }
        HOLDER.set(session);
    }

    public static void clear() {
        HOLDER.remove();
        STAGE_HOLDER.remove();
    }

    public static ChatStreamSession current() {
        return HOLDER.get();
    }

    public static void send(String uuid, Consumer<StreamChatResponse.StreamChatResponseBuilder> customizer) {
        ChatStreamSession session = HOLDER.get();
        if (session == null) {
            return;
        }
        session.send(uuid, customizer);
    }

    public static void send(Consumer<StreamChatResponse.StreamChatResponseBuilder> customizer) {
        ChatStreamSession session = HOLDER.get();
        if (session == null) {
            return;
        }
        session.send(customizer);
    }

    public static void markStage(ChatStageEnum stage) {
        ChatStreamSession session = HOLDER.get();
        if (session != null && stage != null) {
            session.markStage(stage);
        }
        if (stage == null) {
            STAGE_HOLDER.remove();
        } else {
            STAGE_HOLDER.set(stage);
        }
    }

    public static ChatStageEnum currentStage() {
        ChatStageEnum stage = STAGE_HOLDER.get();
        if (stage != null) {
            return stage;
        }
        ChatStreamSession session = HOLDER.get();
        return session == null ? null : session.currentStage().orElse(null);
    }

    public static void stage(ChatStageEnum stage, String content, boolean finished) {
        ChatStreamSession session = HOLDER.get();
        if (session == null || stage == null) {
            return;
        }
        session.sendStage(stage, content, finished);
    }

    public static void stage(ChatStageEnum stage, String content, boolean finished,
            Consumer<StreamChatResponse.StreamChatResponseBuilder> customizer) {
        ChatStreamSession session = HOLDER.get();
        if (session == null || stage == null) {
            return;
        }
        session.sendStage(stage, content, finished, customizer);
    }
}
