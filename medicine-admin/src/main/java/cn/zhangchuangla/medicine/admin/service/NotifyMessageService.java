package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageSendRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageSystemPushRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.NotifyMessageDetailVo;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import cn.zhangchuangla.medicine.model.mq.NotifyMessagePushMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface NotifyMessageService extends IService<NotifyMessage> {

    /**
     * 管理端分页查询通知消息。
     */
    Page<NotifyMessage> listNotifyMessages(NotifyMessageListRequest request);

    /**
     * 管理端查看通知消息详情。
     */
    NotifyMessageDetailVo getNotifyMessageDetail(Long id);

    /**
     * 管理端发送通知消息。
     */
    boolean sendAdminMessage(NotifyMessageSendRequest request);

    /**
     * 管理端编辑通知消息。
     */
    boolean updateAdminMessage(NotifyMessageUpdateRequest request);

    /**
     * 系统推送服务：消息统一走 MQ。
     */
    boolean pushMessageAsync(NotifyMessageSystemPushRequest request);

    /**
     * MQ 消费：处理通知消息落库与用户关联。
     */
    void handlePushMessage(NotifyMessagePushMessage message);

    /**
     * 管理端删除通知消息，仅允许删除管理员发送的消息。
     */
    boolean deleteAdminMessages(java.util.List<Long> ids);

}
