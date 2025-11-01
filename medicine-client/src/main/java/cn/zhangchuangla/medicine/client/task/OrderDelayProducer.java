package cn.zhangchuangla.medicine.client.task;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderDelayProducer {

    private final RedissonClient redissonClient;

    public OrderDelayProducer(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 将订单加入延迟队列，延迟后由消费者拉取执行关闭逻辑
     *
     * @param orderNo      订单编号
     * @param delayMinutes 延迟分钟数
     */
    public void addOrderToDelayQueue(String orderNo, long delayMinutes) {
        RBlockingDeque<String> blockingDeque =
                redissonClient.getBlockingDeque(OrderDelayQueueConstants.ORDER_PAYMENT_TIMEOUT_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(orderNo, delayMinutes, TimeUnit.MINUTES);
        log.info("订单 {} 已加入延迟队列，延迟 {} 分钟", orderNo, delayMinutes);
    }
}
