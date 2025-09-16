package cn.zhangchuangla.medicine.service;

import cn.zhangchuangla.medicine.model.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MessageService extends IService<Message> {

    /**
     * 保存用户消息
     */
    Message saveUserMessage(Long conversationId, String content);

    /**
     * 保存助手消息
     */
    Message saveAssistantMessage(Long conversationId, String content);

    /**
     * 获取会话的消息历史
     */
    List<Message> getConversationMessages(Long conversationId, Integer limit);

    /**
     * 获取会话的完整消息历史
     */
    List<Message> getConversationMessages(Long conversationId);
}
