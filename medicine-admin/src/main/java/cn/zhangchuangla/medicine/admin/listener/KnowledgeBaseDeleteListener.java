package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.common.rabbitmq.message.KnowledgeBaseDeleteMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 知识库删除异步任务消费者，批量清理文档及切片。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseDeleteListener {

    private final KnowledgeBaseService knowledgeBaseService;

    @RabbitListener(queues = KnowledgeBaseQueueConstants.KB_DELETE_QUEUE)
    public void handle(KnowledgeBaseDeleteMessage message) {
        if (message == null || message.getKnowledgeBaseId() == null) {
            log.warn("Skip kb delete message, payload is invalid: {}", message);
            return;
        }
        try {
            knowledgeBaseService.deleteKnowledgeBaseData(message.getKnowledgeBaseId(), message.getBatchSize());
        } catch (Exception ex) {
            log.error("知识库数据删除失败, kbId={}, batchSize={}", message.getKnowledgeBaseId(), message.getBatchSize(), ex);
            throw ex;
        }
    }
}

