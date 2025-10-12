package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.core.common.utils.UUIDUtils;
import cn.zhangchuangla.medicine.common.core.model.entity.Message;
import cn.zhangchuangla.medicine.common.core.model.enums.MessageRoleEnum;
import cn.zhangchuangla.medicine.mapper.MessageMapper;
import cn.zhangchuangla.medicine.service.MessageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Chuang
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveUserMessage(Long conversationId, String content) {
        Message message = new Message();
        message.setUuid(UUIDUtils.simple());
        message.setConversationId(conversationId);
        message.setRole(MessageRoleEnum.USER.getCode());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setIsDelete(0);
        save(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message saveAssistantMessage(Long conversationId, String content) {
        Message message = new Message();
        message.setUuid(UUIDUtils.simple());
        message.setConversationId(conversationId);
        message.setRole(MessageRoleEnum.ASSISTANT.getCode());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setIsDelete(0);
        save(message);
        return message;
    }

    @Override
    public List<Message> getConversationMessages(Long conversationId, Integer limit) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getIsDelete, 0)
                .orderByAsc(Message::getCreateTime);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        return list(queryWrapper);
    }

    @Override
    public List<Message> getConversationMessages(Long conversationId) {
        return getConversationMessages(conversationId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getConversationMessagesCursor(Long conversationId, Long cursor, Integer limit) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getIsDelete, 0);

        if (cursor != null) {
            queryWrapper.lt(Message::getId, cursor);
        }

        queryWrapper.orderByDesc(Message::getId)
                .last("LIMIT " + limit);

        return list(queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMoreMessages(Long conversationId, Long lastMessageId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getIsDelete, 0)
                .lt(Message::getId, lastMessageId);

        return count(queryWrapper) > 0;
    }
}




