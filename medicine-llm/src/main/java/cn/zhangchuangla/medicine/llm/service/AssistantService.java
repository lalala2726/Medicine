package cn.zhangchuangla.medicine.llm.service;

import cn.zhangchuangla.medicine.llm.model.enums.MessageRole;
import cn.zhangchuangla.medicine.llm.model.response.ChatResponse;
import cn.zhangchuangla.medicine.llm.tool.AdminAssistantTools;
import cn.zhangchuangla.medicine.llm.tool.ClientConsultationTools;
import cn.zhangchuangla.medicine.llm.tool.CommonTools;
import cn.zhangchuangla.medicine.llm.utils.SseMessageInjector;
import cn.zhangchuangla.medicine.llm.utils.SseStreamBridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
@Slf4j
public class AssistantService {


    private final ChatClient chatClient;
    private final ClientConsultationTools clientConsultationTools;
    private final SseStreamBridge sseStreamBridge;
    private final SseMessageInjector sseMessageInjector;
    private final AdminAssistantTools adminAssistantTools;
    private final CommonTools commonTools;
    private final VectorStore vectorStore;

    public SseEmitter ClientConsultation(String question) {
        return simpleConsultationSession(question).emitter();
    }


    /**
     * 简单咨询会话
     *
     * @param question 问题
     * @return SSE 会话
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

    /**
     * 管理员助手会话
     *
     * @param userMessage 用户消息
     * @return SSE 会话
     */
    public SseEmitter AdminAssistantChat(String userMessage) {

        String search = search(userMessage); // 这是你的 RAG 检索结果（文本）

        Flux<ChatResponse> stream = chatClient
                .prompt("""
                        使用中文回答用户的问题。
                        
                        你是一名后台管理系统的智能助手，现在处于开发阶段。
                        你必须遵循以下要求：
                        
                        1. 优先基于系统提供的知识内容（RAG 检索结果）回答问题。
                        2. 如果检索结果不足或无法覆盖用户问题，请明确告知“不清楚”或“需要补充数据”，禁止编造内容。
                        3. 你必须诚实、透明，不能杜撰信息。
                        4. 对用户问题进行逐点分析，并给出准确、清晰、简洁的回答。
                        5. 如果用户的问题涉及系统功能、代码、架构、API、数据库设计，请给出现阶段真实情况。
                        6. 如果用户提出需求或问题不明确，你应主动提出澄清建议。
                        
                        以下是系统检索到的相关内容（可能为空）：
                        ----
                        %s
                        ----
                        
                        请基于以上内容回答用户问题。
                        """.formatted(search))
                .tools(adminAssistantTools, commonTools)
                .user(userMessage)
                .stream()
                .content()
                .map(this::toResponse)
                .contextCapture();
        log.info("检索结果：{}", search);
        SseStreamBridge.SseSession session = sseStreamBridge.bridge(stream, sseMessageInjector::clear);
        sseMessageInjector.attach(session);
        return session.emitter();
    }


    public String search(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .build()
        ).toString();
    }


    private ChatResponse toResponse(String content) {
        ChatResponse response = new ChatResponse();
        response.setRole(MessageRole.ASSISTANT);
        response.setContent(content);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
