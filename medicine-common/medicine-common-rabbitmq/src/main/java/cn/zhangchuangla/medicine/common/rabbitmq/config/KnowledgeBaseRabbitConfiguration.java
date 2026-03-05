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
    public Queue knowledgeBaseImportCommandQueue() {
        return QueueBuilder.durable(KnowledgeBaseQueueConstants.COMMAND_QUEUE).build();
    }

    @Bean
    public Queue knowledgeBaseImportResultQueue() {
        return QueueBuilder.durable(KnowledgeBaseQueueConstants.RESULT_QUEUE).build();
    }

    @Bean
    public Binding knowledgeBaseImportCommandBinding(Queue knowledgeBaseImportCommandQueue, DirectExchange knowledgeBaseImportExchange) {
        return BindingBuilder.bind(knowledgeBaseImportCommandQueue)
                .to(knowledgeBaseImportExchange)
                .with(KnowledgeBaseQueueConstants.ROUTING_COMMAND);
    }

    @Bean
    public Binding knowledgeBaseImportResultBinding(Queue knowledgeBaseImportResultQueue, DirectExchange knowledgeBaseImportExchange) {
        return BindingBuilder.bind(knowledgeBaseImportResultQueue)
                .to(knowledgeBaseImportExchange)
                .with(KnowledgeBaseQueueConstants.ROUTING_RESULT);
    }

    @Bean
    public Queue knowledgeBaseVectorDeleteQueue() {
        return QueueBuilder.durable(KnowledgeBaseQueueConstants.VECTOR_DELETE_QUEUE).build();
    }

    @Bean
    public Binding knowledgeBaseVectorDeleteBinding(Queue knowledgeBaseVectorDeleteQueue,
                                                    DirectExchange knowledgeBaseImportExchange) {
        return BindingBuilder.bind(knowledgeBaseVectorDeleteQueue)
                .to(knowledgeBaseImportExchange)
                .with(KnowledgeBaseQueueConstants.ROUTING_VECTOR_DELETE);
    }

    @Bean
    public Queue knowledgeBaseDeleteQueue() {
        return QueueBuilder.durable(KnowledgeBaseQueueConstants.KB_DELETE_QUEUE).build();
    }

    @Bean
    public Binding knowledgeBaseDeleteBinding(Queue knowledgeBaseDeleteQueue,
                                              DirectExchange knowledgeBaseImportExchange) {
        return BindingBuilder.bind(knowledgeBaseDeleteQueue)
                .to(knowledgeBaseImportExchange)
                .with(KnowledgeBaseQueueConstants.ROUTING_KB_DELETE);
    }

    @Bean
    public Queue knowledgeBaseChunkUpdateQueue() {
        return QueueBuilder.durable(KnowledgeBaseQueueConstants.CHUNK_UPDATE_QUEUE).build();
    }

    @Bean
    public Binding knowledgeBaseChunkUpdateBinding(Queue knowledgeBaseChunkUpdateQueue,
                                                   DirectExchange knowledgeBaseImportExchange) {
        return BindingBuilder.bind(knowledgeBaseChunkUpdateQueue)
                .to(knowledgeBaseImportExchange)
                .with(KnowledgeBaseQueueConstants.ROUTING_CHUNK_UPDATE);
    }
}
