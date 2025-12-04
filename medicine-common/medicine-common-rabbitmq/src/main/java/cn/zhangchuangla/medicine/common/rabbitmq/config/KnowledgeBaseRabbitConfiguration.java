package cn.zhangchuangla.medicine.common.rabbitmq.config;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.KnowledgeBaseQueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库导入的基础交换机/队列配置。
 */
@Configuration
public class KnowledgeBaseRabbitConfiguration {

    @Bean
    public DirectExchange knowledgeBaseImportExchange() {
        return ExchangeBuilder
                .directExchange(KnowledgeBaseQueueConstants.EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue knowledgeBaseImportQueue() {
        return QueueBuilder.durable(KnowledgeBaseQueueConstants.QUEUE).build();
    }

    @Bean
    public Binding knowledgeBaseImportBinding(Queue knowledgeBaseImportQueue, DirectExchange knowledgeBaseImportExchange) {
        return BindingBuilder.bind(knowledgeBaseImportQueue)
                .to(knowledgeBaseImportExchange)
                .with(KnowledgeBaseQueueConstants.ROUTING_IMPORT);
    }
}
