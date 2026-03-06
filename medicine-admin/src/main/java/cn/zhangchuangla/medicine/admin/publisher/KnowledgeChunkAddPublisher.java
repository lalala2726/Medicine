package cn.zhangchuangla.medicine.admin.publisher;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkAddCommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 文档切片新增消息发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeChunkAddPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布切片新增 command 消息。
     *
     * @param message 切片新增 command 消息
     */
    public void publishCommand(KnowledgeChunkAddCommandMessage message) {
        if (message == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "切片新增消息不能为空");
        }
        try {
            rabbitTemplate.convertAndSend(
                    KnowledgeBaseQueueConstants.CHUNK_ADD_EXCHANGE,
                    KnowledgeBaseQueueConstants.ROUTING_CHUNK_ADD_COMMAND,
                    message
            );
            log.info("发布切片新增 command 成功, task_uuid={}, chunk_id={}, document_id={}",
                    message.getTask_uuid(), message.getChunk_id(), message.getDocument_id());
        } catch (Exception ex) {
            log.error("发布切片新增 command 失败, task_uuid={}, chunk_id={}, document_id={}",
                    message.getTask_uuid(), message.getChunk_id(), message.getDocument_id(), ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "发送切片新增消息失败: " + ex.getMessage());
        }
    }
}
