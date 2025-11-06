package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallOrderItemService extends IService<MallOrderItem> {


    /**
     * 根据订单ID列表查询订单项
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<MallOrderItem> getOrderItemByOrderId(Long orderId);
}
