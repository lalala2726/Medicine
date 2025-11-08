package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallOrderShipping;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 订单物流服务
 *
 * @author Chuang
 * created 2025/11/08
 */
public interface MallOrderShippingService extends IService<MallOrderShipping> {

    /**
     * 根据订单ID查询物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息
     */
    MallOrderShipping getByOrderId(Long orderId);
}

