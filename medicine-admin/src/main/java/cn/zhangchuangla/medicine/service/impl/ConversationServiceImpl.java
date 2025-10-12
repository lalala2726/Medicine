package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.core.model.entity.Conversation;
import cn.zhangchuangla.medicine.mapper.ConversationMapper;
import cn.zhangchuangla.medicine.service.ConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chuang
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConversation(Conversation conversation) {
        return save(conversation);
    }
}




