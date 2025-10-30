package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.OrderCreateRequest;
import cn.zhangchuangla.medicine.client.model.vo.OrderCreateVo;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Chuang
 */
public interface MallOrderService extends IService<MallOrder> {

    /**
     * 创建订单
     *
     * @param request 创建订单请求参数
     * @return 创建订单结果
     */
    OrderCreateVo createOrder(OrderCreateRequest request);

    /**
     * 查询订单的支付关键信息（金额、状态等），用于唤起支付宝支付。
     *
     * @param orderNo 订单编号
     * @return 支付信息
     */
    OrderCreateVo getOrderPayInfo(String orderNo);
}
