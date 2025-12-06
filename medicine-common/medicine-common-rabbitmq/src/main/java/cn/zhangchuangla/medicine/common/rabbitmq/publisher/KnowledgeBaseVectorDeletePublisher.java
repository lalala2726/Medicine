package cn.zhangchuangla.medicine.common.rabbitmq.publisher;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseVectorDeleteMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 知识库向量异步删除发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseVectorDeletePublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布删除向量消息，异步清理 Milvus 中的向量数据。
     */
    public void publish(Integer knowledgeBaseId, List<String> vectorIds, Long documentId) {
        if (knowledgeBaseId == null || CollectionUtils.isEmpty(vectorIds)) {
            log.warn("Skip publish kb vector delete event, invalid payload. kbId={}, vectors={}", knowledgeBaseId, vectorIds);
            return;
        }
        KnowledgeBaseVectorDeleteMessage message = KnowledgeBaseVectorDeleteMessage.builder()
                .knowledgeBaseId(knowledgeBaseId)
                .vectorIds(vectorIds)
                .documentId(documentId)
                .build();
        rabbitTemplate.convertAndSend(
                KnowledgeBaseQueueConstants.EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_VECTOR_DELETE,
                message
        );
    }
}

