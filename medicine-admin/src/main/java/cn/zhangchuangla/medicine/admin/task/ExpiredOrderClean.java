package cn.zhangchuangla.medicine.admin.task;

import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.zhangchuangla.medicine.common.core.constants.Constants.ORDER_TIMEOUT_MINUTES;

/**
 * 过期订单清理定时任务
 *
 * @author Chuang
 * <p>
 * created on 2025/11/5 
 */
@Slf4j
@Component
public class ExpiredOrderClean {

    private final MallOrderService mallOrderService;

    public ExpiredOrderClean(MallOrderService mallOrderService) {
        this.mallOrderService = mallOrderService;
    }


    /**
     * 定时清理过期订单任务
     * <p>
     * 尽管订单创建时已通过Redisson延迟队列实现自动过期清理机制，但为确保订单状态一致性，
     * 本任务作为补偿机制定期扫描并更新超时订单状态。
     * <p>
     * 更新操作采用乐观锁机制，通过version字段保证数据并发安全。
     * 考虑到系统负载及业务量，清理任务安排在凌晨执行，避免影响正常业务流程。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clean() {
        long expiredTime = System.currentTimeMillis() - 1000 * 60 * ORDER_TIMEOUT_MINUTES;
        List<MallOrder> expiredOrders = mallOrderService.getExpiredOrderClean(expiredTime);
        expiredOrders.forEach(order -> {
            order.setOrderStatus(OrderStatusEnum.EXPIRED.getType());
            boolean result = mallOrderService.updateById(order);
            if (result) {
                log.info("订单{}已过期，已更新状态为{}", order.getOrderNo(), OrderStatusEnum.EXPIRED.getType());
            }
        });
    }

}
