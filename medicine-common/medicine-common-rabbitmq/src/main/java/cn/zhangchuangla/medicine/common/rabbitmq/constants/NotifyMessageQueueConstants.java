package cn.zhangchuangla.medicine.common.rabbitmq.constants;

/**
 * 通知消息相关的交换机、队列与路由键常量。
 */
public final class NotifyMessageQueueConstants {

    /**
     * 通知消息交换机。
     */
    public static final String EXCHANGE = "medicine.notify.message.exchange";

    /**
     * 通知消息队列。
     */
    public static final String QUEUE = "medicine.notify.message.queue";

    /**
     * 通知消息路由键。
     */
    public static final String ROUTING_SEND = "notify.message.send";

    private NotifyMessageQueueConstants() {
    }
}
