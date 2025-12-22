package cn.zhangchuangla.medicine.client.task;

import cn.zhangchuangla.medicine.client.service.MallOrderService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class OrderDelayConsumer implements CommandLineRunner {

    private final RedissonClient redissonClient;
    private final MallOrderService mallOrderService;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread consumerThread;

    public OrderDelayConsumer(RedissonClient redissonClient, MallOrderService mallOrderService) {
        this.redissonClient = redissonClient;
        this.mallOrderService = mallOrderService;
    }

    @Override
    public void run(String @NonNull ... args) {
        consumerThread = new Thread(() -> {
            RBlockingDeque<String> blockingDeque =
                    redissonClient.getBlockingDeque(OrderDelayQueueConstants.ORDER_PAYMENT_TIMEOUT_QUEUE);
            while (running.get() && !redissonClient.isShuttingDown() && !redissonClient.isShutdown()) {
                String orderNo = null;
                try {
                    orderNo = blockingDeque.take();
                    mallOrderService.closeOrderIfUnpaid(orderNo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    if (redissonClient.isShuttingDown() || redissonClient.isShutdown()) {
                        break;
                    }
                    log.error("订单 {} 关闭失败，错误信息：{}", orderNo, e.getMessage());
                }
            }
        }, "order-payment-timeout-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @jakarta.annotation.PreDestroy
    public void stop() {
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

}
