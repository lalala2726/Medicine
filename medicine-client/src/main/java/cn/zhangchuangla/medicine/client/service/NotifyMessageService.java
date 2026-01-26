package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.client.model.vo.NotifyMessageDetailVo;
import cn.zhangchuangla.medicine.client.model.vo.NotifyMessageListVo;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface NotifyMessageService extends IService<NotifyMessage> {

    /**
     * 获取当前用户通知消息列表。
     */
    Page<NotifyMessageListVo> listUserMessages(NotifyMessageListRequest request);

    /**
     * 获取通知消息详情并自动标记已读。
     */
    NotifyMessageDetailVo getMessageDetail(Long notifyId);

    /**
     * 删除当前用户通知消息。
     */
    boolean deleteMessage(Long notifyId);

}
