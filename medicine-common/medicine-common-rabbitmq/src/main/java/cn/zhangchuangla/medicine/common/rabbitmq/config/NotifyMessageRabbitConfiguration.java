package cn.zhangchuangla.medicine.common.rabbitmq.config;

import cn.zhangchuangla.medicine.common.rabbitmq.constants.NotifyMessageQueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知消息队列基础配置。
 */
@Configuration
public class NotifyMessageRabbitConfiguration {

    @Bean
    public DirectExchange notifyMessageExchange() {
        return ExchangeBuilder
                .directExchange(NotifyMessageQueueConstants.EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notifyMessageQueue() {
        return QueueBuilder.durable(NotifyMessageQueueConstants.QUEUE).build();
    }

    @Bean
    public Binding notifyMessageBinding(Queue notifyMessageQueue, DirectExchange notifyMessageExchange) {
        return BindingBuilder.bind(notifyMessageQueue)
                .to(notifyMessageExchange)
                .with(NotifyMessageQueueConstants.ROUTING_SEND);
    }
}
