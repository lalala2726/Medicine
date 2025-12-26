package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.NotifyMessageService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.NotifyMessageQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.NotifyMessagePushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知消息推送消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyMessagePushListener {

    private final NotifyMessageService notifyMessageService;

    @RabbitListener(queues = NotifyMessageQueueConstants.QUEUE)
    public void handle(NotifyMessagePushMessage message) {
        if (message == null) {
            log.warn("Skip notify message push, message is null");
            return;
        }
        // 消费 MQ，统一执行落库与分批写入用户关联。
        notifyMessageService.handlePushMessage(message);
    }
}
