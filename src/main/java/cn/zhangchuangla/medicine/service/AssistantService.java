package cn.zhangchuangla.medicine.service;

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
     * 获取会话历史
     * @param uuid 会话UUID
     * @param limit 返回条数，默认50
     */
    ChatHistoryResponse history(String uuid, Integer limit);

}
