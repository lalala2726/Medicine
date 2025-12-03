package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
import cn.zhangchuangla.medicine.llm.prompt.SystemPrompt;
import cn.zhangchuangla.medicine.llm.tool.AdminAssistantTools;
import cn.zhangchuangla.medicine.llm.tool.ClientConsultationTools;
import cn.zhangchuangla.medicine.llm.tool.CommonTools;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import cn.zhangchuangla.medicine.llm.utils.SseStreamBridge;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import static cn.zhangchuangla.medicine.llm.prompt.SystemPrompt.CONSULTATION_SYSTEM_PROMPT;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/22
 */
@Service
@RequiredArgsConstructor
public class AssistantService {


    private final ChatClient chatClient;
    private final ClientConsultationTools clientConsultationTools;
    private final SseStreamBridge sseStreamBridge;
    private final SseMessageInjector sseMessageInjector;
    private final AdminAssistantTools adminAssistantTools;
    private final CommonTools commonTools;

    public SseEmitter ClientConsultation(String question) {
        return simpleConsultationSession(question).emitter();
    }

    /**
     * 返回可手动插入消息的 SSE 会话包装。
     */
    public SseStreamBridge.SseSession simpleConsultationSession(String question) {
        Flux<ChatResponse> stream = chatClient
                .prompt()
                .system(CONSULTATION_SYSTEM_PROMPT)
                .user(question)
                .tools(clientConsultationTools)
                .stream()
                .content()
                .map(this::toResponse)
                .contextCapture();

        SseStreamBridge.SseSession session = sseStreamBridge.bridge(stream, sseMessageInjector::clear);
        sseMessageInjector.attach(session);
        return session;
    }

    public SseEmitter chat(String userMessage) {
        Flux<ChatResponse> stream = chatClient.prompt()
                .tools(adminAssistantTools, commonTools)
                .system(SystemPrompt.ADMIN_ASSISTANT_PROMPT)
                .user(userMessage)
                .stream()
                .content()
                .map(this::toResponse)
                .contextCapture();

        SseStreamBridge.SseSession session = sseStreamBridge.bridge(stream, sseMessageInjector::clear);
        sseMessageInjector.attach(session);
        return session.emitter();
    }

    private ChatResponse toResponse(String content) {
        ChatResponse response = new ChatResponse();
        response.setRole(MessageRole.ASSISTANT);
        response.setContent(content);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
