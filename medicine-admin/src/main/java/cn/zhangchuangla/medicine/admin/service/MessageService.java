package cn.zhangchuangla.medicine.admin.service;

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

    /**
     * 使用游标分页获取会话消息历史
     *
     * @param conversationId 会话ID
     * @param cursor         游标（消息ID），为null时从最新消息开始
     * @param limit          每页条数
     * @return 消息列表（按时间倒序）
     */
    List<Message> getConversationMessagesCursor(Long conversationId, Long cursor, Integer limit);

    /**
     * 检查是否还有更多消息
     *
     * @param conversationId 会话ID
     * @param lastMessageId  当前最后一条消息的ID
     * @return 是否还有更多消息
     */
    boolean hasMoreMessages(Long conversationId, Long lastMessageId);
}
