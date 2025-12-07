package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseChunkUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 文档切片更新异步消费者，重算向量。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseChunkUpdateListener {

    private final KnowledgeBaseService knowledgeBaseService;

    @RabbitListener(queues = KnowledgeBaseQueueConstants.CHUNK_UPDATE_QUEUE)
    public void handle(KnowledgeBaseChunkUpdateMessage message) {
        if (message == null || message.getKnowledgeBaseId() == null || message.getVectorId() == null) {
            log.warn("Skip chunk update message, payload invalid: {}", message);
            return;
        }
        try {
            knowledgeBaseService.updateDocumentChunkVector(message);
        } catch (Exception ex) {
            log.error("切片向量更新失败, chunkId={}, vectorId={}", message.getChunkId(), message.getVectorId(), ex);
            throw ex;
        }
    }
}

