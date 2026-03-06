package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentChunkService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkAddResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 文档切片新增结果消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeChunkAddResultListener {

    private final KbDocumentChunkService kbDocumentChunkService;

    /**
     * 消费 AI 回传的切片新增结果消息，并委托服务层处理。
     *
     * @param message AI 回传的结果消息
     */
    @RabbitListener(queues = KnowledgeBaseQueueConstants.CHUNK_ADD_RESULT_QUEUE, concurrency = "1")
    public void handle(KnowledgeChunkAddResultMessage message) {
        if (message == null) {
            log.warn("跳过切片新增结果消息: message is null");
            return;
        }
        kbDocumentChunkService.handleChunkAddResult(message);
    }
}
