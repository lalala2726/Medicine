package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 单切片重建结果消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeChunkRebuildResultListener {

    private final KbDocumentChunkService kbDocumentChunkService;

    /**
     * 消费 AI 回传的单切片重建结果消息，并委托服务层处理判旧与状态回写。
     *
     * @param message AI 回传的结果消息
     */
    @RabbitListener(queues = KnowledgeBaseQueueConstants.CHUNK_REBUILD_RESULT_QUEUE, concurrency = "1")
    public void handle(KnowledgeChunkRebuildResultMessage message) {
        if (message == null) {
            log.warn("跳过切片重建结果消息: message is null");
            return;
        }
        kbDocumentChunkService.handleChunkRebuildResult(message);
    }
}
