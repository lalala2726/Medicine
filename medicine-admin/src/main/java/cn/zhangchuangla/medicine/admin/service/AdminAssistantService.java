package cn.zhangchuangla.medicine.admin.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
public interface AdminAssistantService {

    /**
     * 聊天
     *
     * @param request 请求（包含消息与文件）
     * @return sse
     */
    SseEmitter chat(cn.zhangchuangla.medicine.llm.model.request.AssistantChatRequest request);
}
