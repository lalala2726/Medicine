package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.*;
import cn.zhangchuangla.medicine.client.model.vo.OrderCheckoutVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderListVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderPreviewVo;
import cn.zhangchuangla.medicine.model.dto.AlipayNotifyDTO;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.vo.mall.OrderShippingVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Chuang
 */
public interface MallOrderService extends IService<MallOrder> {

    /**
     * 支付宝异步通知回调
     *
     * @param alipayNotifyDTO 支付宝异步通知参数
     * @return 处理结果
     */
    String alipayNotify(AlipayNotifyDTO alipayNotifyDTO, HttpServletRequest request);

    /**
     * 关闭未支付订单
     *
     * @param orderNo 订单编号
     */
    void closeOrderIfUnpaid(String orderNo);

    /**
     * 用户确认收货
     *
     * @param request 确认收货请求参数
     * @return 是否成功
     */
    boolean confirmReceipt(OrderReceiveRequest request);

    /**
     * 查询订单物流信息
     *
     * @param orderNo 订单编号
     * @return 物流信息
     */
    OrderShippingVo getOrderShipping(String orderNo);

    /**
     * 分页查询用户订单列表
     *
     * @param request 查询条件
     * @return 订单列表
     */
    Page<OrderListVo> getOrderList(OrderListRequest request);

    /**
     * 查询订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    OrderDetailVo getOrderDetail(String orderNo);

    /**
     * 从购物车创建订单并支付
     * <p>
     * 用户可以选择购物车中的多个商品进行结算，系统会校验商品状态和库存，
     * 扣减库存后创建订单，并根据支付方式直接处理支付，自动删除已结算的购物车商品
     * </p>
     *
     * @param request 购物车结算请求
     * @return 订单结算结果
     */
    OrderCheckoutVo createOrderFromCart(CartSettleRequest request);

    /**
     * 订单结算（创建订单并支付）
     * <p>
     * 整合了订单创建和支付流程，用户提交订单时直接选择支付方式：
     * - 钱包支付：同步扣款，订单状态变为已支付
     * - 支付宝支付：生成支付表单，订单状态为待支付
     * </p>
     *
     * @param request 订单结算请求参数
     * @return 订单结算结果
     */
    OrderCheckoutVo checkoutOrder(OrderCheckoutRequest request);

    /**
     * 订单预览
     * <p>
     * 在用户提交订单前预览订单信息,包括商品详情、价格、运费等,
     * 支持单个商品购买和购物车结算两种场景
     * </p>
     *
     * @param request 订单预览请求参数
     * @return 订单预览信息
     */
    OrderPreviewVo previewOrder(OrderPreviewRequest request);
}
