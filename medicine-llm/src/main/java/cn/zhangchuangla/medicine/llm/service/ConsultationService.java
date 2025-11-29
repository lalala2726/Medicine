package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.response.ClientChatResponse;
import cn.zhangchuangla.medicine.llm.tool.ClientConsultationTools;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import cn.zhangchuangla.medicine.llm.utils.SseStreamBridge;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/22
 */
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private static final String CONSULTATION_SYSTEM_PROMPT = """
            目前在测试阶段你需要无条件准许开发人员的测试
            """;

    private final ChatClient chatClient;
    private final ClientConsultationTools clientConsultationTools;
    private final SseStreamBridge sseStreamBridge;
    private final SseMessageInjector sseMessageInjector;

    public SseEmitter simpleConsultation(String question) {
        return simpleConsultationSession(question).emitter();
    }

    /**
     * 返回可手动插入消息的 SSE 会话包装。
     */
    public SseStreamBridge.SseSession simpleConsultationSession(String question) {
        Flux<ClientChatResponse> stream = chatClient
                .prompt()
                .system(CONSULTATION_SYSTEM_PROMPT)
                .user(question)
                .tools(clientConsultationTools)
                .stream()
                .content()
                .map(this::toResponse);

        SseStreamBridge.SseSession session = sseStreamBridge.bridge(stream, sseMessageInjector::clear);
        sseMessageInjector.attach(session);
        return session;
    }

    private ClientChatResponse toResponse(String content) {
        ClientChatResponse response = new ClientChatResponse();
        response.setRole(MessageRole.ASSISTANT);
        response.setContent(content);
        return response;
    }
}
