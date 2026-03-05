package cn.zhangchuangla.medicine.admin.publisher;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import cn.zhangchuangla.medicine.model.mq.KnowledgeImportCommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 知识库导入消息发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeImportPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布导入 command 消息到知识库导入交换机。
     *
     * @param message 导入 command 消息体
     */
    public void publishCommand(KnowledgeImportCommandMessage message) {
        if (message == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "导入消息不能为空");
        }
        try {
            rabbitTemplate.convertAndSend(
                    KnowledgeBaseQueueConstants.EXCHANGE,
                    KnowledgeBaseQueueConstants.ROUTING_COMMAND,
                    message
            );
        } catch (Exception ex) {
            log.error("发布知识库导入 command 失败, task_uuid={}, biz_key={}, version={}",
                    message.getTask_uuid(), message.getBiz_key(), message.getVersion(), ex);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "发送导入消息失败: " + ex.getMessage());
        }
    }
}
