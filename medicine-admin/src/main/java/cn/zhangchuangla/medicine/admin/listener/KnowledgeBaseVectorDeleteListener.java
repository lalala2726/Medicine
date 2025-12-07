package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseVectorDeleteMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 订阅向量删除消息，异步清理 Milvus 中的文档向量。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseVectorDeleteListener {

    private final KnowledgeBaseService knowledgeBaseService;

    @RabbitListener(queues = KnowledgeBaseQueueConstants.VECTOR_DELETE_QUEUE)
    public void handle(KnowledgeBaseVectorDeleteMessage message) {
        if (message == null || message.getKnowledgeBaseId() == null || CollectionUtils.isEmpty(message.getVectorIds())) {
            log.warn("Skip kb vector delete message, payload is invalid: {}", message);
            return;
        }
        try {
            knowledgeBaseService.deleteDocumentVectors(message.getKnowledgeBaseId(), message.getVectorIds());
        } catch (Exception ex) {
            log.error("向量删除失败, kbId={}, docId={}, vectors={}",
                    message.getKnowledgeBaseId(), message.getDocumentId(), message.getVectorIds(), ex);
            throw ex;
        }
    }
}

