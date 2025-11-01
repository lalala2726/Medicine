package cn.zhangchuangla.medicine.client.task;

/**
 * 延迟队列常量，确保生产者与消费者使用同一个阻塞队列名称。
 */
final class OrderDelayQueueConstants {

    static final String ORDER_PAYMENT_TIMEOUT_QUEUE = "orderTimeoutQueue";

    private OrderDelayQueueConstants() {
    }
}
