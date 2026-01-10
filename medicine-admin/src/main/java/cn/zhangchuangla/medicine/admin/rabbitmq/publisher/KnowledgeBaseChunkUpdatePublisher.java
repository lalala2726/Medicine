package cn.zhangchuangla.medicine.admin.rabbitmq.publisher;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseChunkUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 文档切片更新后触发向量重算的发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseChunkUpdatePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(KnowledgeBaseChunkUpdateMessage message) {
        if (message == null || message.getKnowledgeBaseId() == null || message.getChunkId() == null) {
            log.warn("Skip publish chunk update event, invalid payload: {}", message);
            return;
        }
        rabbitTemplate.convertAndSend(
                KnowledgeBaseQueueConstants.EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_CHUNK_UPDATE,
                message
        );
    }
}
