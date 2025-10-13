package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface ConversationService extends IService<Conversation> {
    /**
     * 新建会话（事务）
     */
    boolean createConversation(Conversation conversation);
}
