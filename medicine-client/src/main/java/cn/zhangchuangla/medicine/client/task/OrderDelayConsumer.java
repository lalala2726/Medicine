package cn.zhangchuangla.medicine.client.task;

import cn.zhangchuangla.medicine.client.service.MallOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderDelayConsumer implements CommandLineRunner{

    private final RedissonClient redissonClient;
    private final MallOrderService mallOrderService;

    public OrderDelayConsumer(RedissonClient redissonClient, MallOrderService mallOrderService) {
        this.redissonClient = redissonClient;
        this.mallOrderService = mallOrderService;
    }

    @Override
    public void run(String... args) {
        Thread consumerThread = new Thread(() -> {
            RBlockingDeque<String> blockingDeque =
                    redissonClient.getBlockingDeque(OrderDelayQueueConstants.ORDER_PAYMENT_TIMEOUT_QUEUE);
            while (true) {
                String orderNo = null;
                try {
                    orderNo = blockingDeque.take();
                    mallOrderService.closeOrderIfUnpaid(orderNo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("订单 {} 关闭失败，错误信息：{}", orderNo, e.getMessage());
                }
            }
        }, "order-payment-timeout-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }


}
