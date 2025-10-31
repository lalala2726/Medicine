package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.OrderConfirmRequest;
import cn.zhangchuangla.medicine.client.model.request.OrderCreateRequest;
import cn.zhangchuangla.medicine.client.model.vo.OrderCreateVo;
import cn.zhangchuangla.medicine.model.dto.AlipayNotifyDTO;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * 确认订单
     *
     * @param request 确认订单请求参数
     * @return 如果是第三方支付方式, 例如支付宝, 则返回第三方支付结果页面的表单信息
     */
    String confirmOrder(OrderConfirmRequest request);



    /**
     * 支付宝异步通知回调
     *
     * @param alipayNotifyDTO 支付宝异步通知参数
     * @return 处理结果
     */
    String alipayNotify(AlipayNotifyDTO alipayNotifyDTO, HttpServletRequest request);
}
