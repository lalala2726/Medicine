package cn.zhangchuangla.medicine.admin.publisher;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeChunkRebuildCommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 单切片重建消息发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeChunkRebuildPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布单切片重建 command 消息。
     *
     * @param message 单切片重建 command 消息
     */
    public void publishCommand(KnowledgeChunkRebuildCommandMessage message) {
        if (message == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "切片重建消息不能为空");
        }
        try {
            rabbitTemplate.convertAndSend(
                    KnowledgeBaseQueueConstants.CHUNK_REBUILD_EXCHANGE,
                    KnowledgeBaseQueueConstants.ROUTING_CHUNK_REBUILD_COMMAND,
                    message
            );
            log.info("发布切片重建 command 成功, task_uuid={}, vector_id={}, version={}",
                    message.getTask_uuid(), message.getVector_id(), message.getVersion());
        } catch (Exception ex) {
            log.error("发布切片重建 command 失败, task_uuid={}, vector_id={}, version={}",
                    message.getTask_uuid(), message.getVector_id(), message.getVersion(), ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "发送切片重建消息失败: " + ex.getMessage());
        }
    }
}
