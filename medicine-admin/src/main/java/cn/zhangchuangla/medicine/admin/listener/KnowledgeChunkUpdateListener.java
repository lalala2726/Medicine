package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 知识库切片同步消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeChunkUpdateListener {

    private final KbDocumentService kbDocumentService;

    @RabbitListener(queues = KnowledgeBaseQueueConstants.CHUNK_UPDATE_QUEUE, concurrency = "1")
    public void handle(KnowledgeImportResultMessage message) {
        if (message == null) {
            log.warn("跳过知识库切片同步消息: message is null");
            return;
        }
        kbDocumentService.handleChunkUpdateResult(message);
    }
}
