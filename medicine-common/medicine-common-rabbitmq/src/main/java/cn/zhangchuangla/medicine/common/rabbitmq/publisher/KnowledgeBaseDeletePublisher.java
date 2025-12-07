package cn.zhangchuangla.medicine.common.rabbitmq.publisher;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseDeleteMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 知识库删除异步任务发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseDeletePublisher {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final RabbitTemplate rabbitTemplate;

    public void publish(Integer knowledgeBaseId, Integer batchSize) {
        if (knowledgeBaseId == null) {
            log.warn("Skip publish kb delete event, kbId is null");
            return;
        }
        int size = batchSize == null || batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;
        KnowledgeBaseDeleteMessage message = KnowledgeBaseDeleteMessage.builder()
                .knowledgeBaseId(knowledgeBaseId)
                .batchSize(size)
                .build();
        rabbitTemplate.convertAndSend(
                KnowledgeBaseQueueConstants.EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_KB_DELETE,
                message
        );
    }
}

