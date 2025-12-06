package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseIngestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 知识库导入异步任务消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseIngestListener {

    private final KnowledgeBaseService knowledgeBaseService;

    @RabbitListener(queues = KnowledgeBaseQueueConstants.QUEUE)
    public void handle(KnowledgeBaseIngestMessage message) {
        if (message == null || message.getKnowledgeBaseId() == null || CollectionUtils.isEmpty(message.getFileUrls())) {
            log.warn("Skip knowledge base ingest message, payload is invalid: {}", message);
            return;
        }
        try {
            knowledgeBaseService.ingestKnowledgeBase(message.getKnowledgeBaseId(), message.getFileUrls(), message.getUsername());
        } catch (Exception ex) {
            log.error("知识库导入失败, kbId={}, files={}", message.getKnowledgeBaseId(), message.getFileUrls(), ex);
            throw ex;
        }
    }
}
