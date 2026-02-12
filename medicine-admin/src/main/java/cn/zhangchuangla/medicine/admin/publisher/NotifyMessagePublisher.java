package cn.zhangchuangla.medicine.admin.publisher;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.NotifyMessageQueueConstants;
import cn.zhangchuangla.medicine.model.mq.NotifyMessagePushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 通知消息发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotifyMessagePushMessage message) {
        if (message == null || !StringUtils.hasText(message.getTitle()) || !StringUtils.hasText(message.getReceiverType())) {
            log.warn("Skip publish notify message, payload invalid: {}", message);
            return;
        }
        // 统一走交换机，便于后续扩展多消费者。
        rabbitTemplate.convertAndSend(
                NotifyMessageQueueConstants.EXCHANGE,
                NotifyMessageQueueConstants.ROUTING_SEND,
                message
        );
    }
}
