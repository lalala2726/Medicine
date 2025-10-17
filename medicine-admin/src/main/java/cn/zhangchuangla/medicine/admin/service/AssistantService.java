package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.assistant.HistoryRequest;
import cn.zhangchuangla.medicine.model.vo.chat.StreamChatResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.ChatHistoryResponse;
import cn.zhangchuangla.medicine.model.vo.llm.chat.UserMessageRequest;
import reactor.core.publisher.Flux;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/16 10:49
 */
public interface AssistantService {

    Flux<StreamChatResponse> chat(UserMessageRequest userMessageRequest);

    /**
     * 获取会话历史（分页）
     *
     * @param request 分页请求参数
     */
    ChatHistoryResponse history(HistoryRequest request);

}
