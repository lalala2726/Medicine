package cn.zhangchuangla.medicine.admin.listener;

import cn.zhangchuangla.medicine.admin.service.KbDocumentService;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 知识库导入结果消息消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeImportResultListener {

    private final KbDocumentService kbDocumentService;

    /**
     * 消费 AI 回传的知识库导入结果消息，并委托服务层处理判旧与状态回写。
     *
     * @param message 导入结果消息
     */
    @RabbitListener(queues = KnowledgeBaseQueueConstants.RESULT_QUEUE)
    public void handle(KnowledgeImportResultMessage message) {
        if (message == null) {
            log.warn("跳过知识库导入结果消息: message is null");
            return;
        }
        kbDocumentService.handleImportResult(message);
    }
}
