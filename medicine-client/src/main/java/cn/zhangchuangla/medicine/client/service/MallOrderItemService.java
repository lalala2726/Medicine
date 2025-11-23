package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallOrderItemService extends IService<MallOrderItem> {

    /**
     * 根据订单ID查询订单项列表。
     */
    List<MallOrderItem> getOrderItemByOrderId(Long orderId);

}
