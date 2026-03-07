package cn.zhangchuangla.medicine.admin.publisher;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkAddCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportCommandMessage;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportResultMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 知识库相关消息统一发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgePublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布导入 command 消息到知识库导入交换机。
     *
     * @param message 导入 command 消息体
     */
    public void publishImportCommand(KnowledgeImportCommandMessage message) {
        assertMessageNotNull(message, "导入消息不能为空");
        send(
                message,
                KnowledgeBaseQueueConstants.EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_COMMAND,
                "知识库导入 command",
                () -> String.format("task_uuid=%s, biz_key=%s, version=%s",
                        message.getTask_uuid(), message.getBiz_key(), message.getVersion()),
                "发送导入消息失败",
                false
        );
    }

    /**
     * 发布切片同步消息到知识库导入交换机。
     *
     * @param message 导入结果消息体
     */
    public void publishImportChunkUpdate(KnowledgeImportResultMessage message) {
        assertMessageNotNull(message, "切片同步消息不能为空");
        send(
                message,
                KnowledgeBaseQueueConstants.EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_CHUNK_UPDATE,
                "知识库切片同步消息",
                () -> String.format("task_uuid=%s, biz_key=%s, version=%s",
                        message.getTask_uuid(), message.getBiz_key(), message.getVersion()),
                "发送切片同步消息失败",
                false
        );
    }

    /**
     * 发布单切片重建 command 消息。
     *
     * @param message 单切片重建 command 消息
     */
    public void publishChunkRebuildCommand(KnowledgeChunkRebuildCommandMessage message) {
        assertMessageNotNull(message, "切片重建消息不能为空");
        send(
                message,
                KnowledgeBaseQueueConstants.CHUNK_REBUILD_EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_CHUNK_REBUILD_COMMAND,
                "切片重建 command",
                () -> String.format("task_uuid=%s, vector_id=%s, version=%s",
                        message.getTask_uuid(), message.getVector_id(), message.getVersion()),
                "发送切片重建消息失败",
                true
        );
    }

    /**
     * 发布切片新增 command 消息。
     *
     * @param message 切片新增 command 消息
     */
    public void publishChunkAddCommand(KnowledgeChunkAddCommandMessage message) {
        assertMessageNotNull(message, "切片新增消息不能为空");
        send(
                message,
                KnowledgeBaseQueueConstants.CHUNK_ADD_EXCHANGE,
                KnowledgeBaseQueueConstants.ROUTING_CHUNK_ADD_COMMAND,
                "切片新增 command",
                () -> String.format("task_uuid=%s, chunk_id=%s, document_id=%s",
                        message.getTask_uuid(), message.getChunk_id(), message.getDocument_id()),
                "发送切片新增消息失败",
                true
        );
    }

    private void assertMessageNotNull(Object message, String errorMessage) {
        if (message == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, errorMessage);
        }
    }

    private void send(
            Object message,
            String exchange,
            String routingKey,
            String operation,
            Supplier<String> logContextSupplier,
            String errorMessage,
            boolean logSuccess
    ) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            if (logSuccess) {
                log.info("发布{}成功, {}", operation, logContextSupplier.get());
            }
        } catch (Exception ex) {
            log.error("发布{}失败, {}", operation, logContextSupplier.get(), ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, errorMessage + ": " + ex.getMessage());
        }
    }
}
