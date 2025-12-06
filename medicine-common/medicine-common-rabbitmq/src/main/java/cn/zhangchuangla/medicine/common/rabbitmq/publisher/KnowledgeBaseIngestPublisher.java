package cn.zhangchuangla.medicine.common.rabbitmq.publisher;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseIngestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 知识库导入异步任务发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseIngestPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(Integer knowledgeBaseId, List<String> fileUrls) {
        publish(knowledgeBaseId, fileUrls, null);
    }

    public void publish(Integer knowledgeBaseId, List<String> fileUrls, String username) {
        if (knowledgeBaseId == null || CollectionUtils.isEmpty(fileUrls)) {
            log.warn("Skip publish knowledge base ingest event, invalid payload. kbId={}, files={}", knowledgeBaseId, fileUrls);
            return;
        }
        KnowledgeBaseIngestMessage message = KnowledgeBaseIngestMessage.builder()
                .knowledgeBaseId(knowledgeBaseId)
                .fileUrls(fileUrls)
                .username(username)
                .build();
        rabbitTemplate.convertAndSend(
                KnowledgeBaseQueueConstants.EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_IMPORT,
                message
        );
    }
}
