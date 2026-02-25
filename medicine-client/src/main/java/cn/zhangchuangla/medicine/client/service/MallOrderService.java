package cn.zhangchuangla.medicine.client.service;

import cn.zhangchuangla.medicine.client.model.request.*;
import cn.zhangchuangla.medicine.client.model.vo.*;
import cn.zhangchuangla.medicine.model.dto.AlipayNotifyDTO;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.vo.OrderShippingVo;
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
     * 用户取消订单
     * <p>
     * 用户主动取消订单，需要提供取消原因。
     * 只有待支付状态的订单可以取消，取消后会恢复库存。
     * </p>
     *
     * @param request 订单取消请求参数
     * @return 是否成功
     */
    boolean cancelOrder(OrderCancelRequest request);

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
     * 从购物车提交订单（创建订单并锁定库存）
     * <p>
     * 用户可以选择购物车中的多个商品进行结算，系统会校验商品状态和库存，
     * 扣减库存后创建订单，订单状态为待支付，自动删除已结算的购物车商品。
     * 订单创建后需要在30分钟内完成支付，否则订单将自动取消并恢复库存。
     * </p>
     *
     * @param request 购物车提交订单请求
     * @return 订单提交结果
     */
    OrderCheckoutVo createOrderFromCart(CartSettleRequest request);

    /**
     * 提交订单（创建订单并锁定库存）
     * <p>
     * 用户提交订单时创建订单并扣减库存，订单状态为待支付。
     * 订单创建后需要在30分钟内完成支付，否则订单将自动取消并恢复库存。
     * </p>
     *
     * @param request 订单提交请求参数
     * @return 订单提交结果
     */
    OrderCheckoutVo checkoutOrder(OrderCheckoutRequest request);

    /**
     * 订单支付
     * <p>
     * 对已创建的待支付订单进行支付操作，支持钱包支付和支付宝支付：
     * - 钱包支付：同步扣款，订单状态变为待发货
     * - 支付宝支付：生成支付表单，订单状态保持待支付，等待异步回调
     * </p>
     *
     * @param request 订单支付请求参数
     * @return 订单支付结果
     */
    OrderPayVo payOrder(OrderPayRequest request);

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

    /**
     * 获取订单支付信息（待支付订单重进支付页使用）。
     *
     * @param request 订单支付信息请求参数
     * @return 支付信息
     */
    OrderPayInfoVo getOrderPayInfo(OrderPayInfoRequest request);
}
