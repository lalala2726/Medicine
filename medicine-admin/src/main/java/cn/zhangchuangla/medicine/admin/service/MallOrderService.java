package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.dto.UserOrderStatistics;
import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Chuang
 */
public interface MallOrderService extends IService<MallOrder> {

    /**
     * 订单列表
     *
     * @param request 查询参数
     * @return 订单列分页结果
     */
    Page<MallOrder> orderList(MallOrderListRequest request);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    MallOrder getOrderByOrderNo(String orderNo);

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单
     */
    MallOrder getOrderById(Long id);

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVo orderDetail(Long orderId);

    /**
     * 更新订单地址
     *
     * @param request 更新参数
     * @return 是否成功
     */
    boolean updateOrderAddress(AddressUpdateRequest request);

    /**
     * 更新订单备注
     *
     * @param request 更新参数
     * @return 是否成功
     */
    boolean updateOrderRemark(RemarkUpdateRequest request);

    /**
     * 更新订单价格
     *
     * @param request 订单价格更新参数
     * @return 是否成功
     */
    boolean updateOrderPrice(OrderUpdatePriceRequest request);

    /**
     * 订单退款
     *
     * @param request 订单退款参数
     * @return 是否成功
     */
    boolean orderRefund(OrderRefundRequest request);

    /**
     * 订单列表（带商品信息）
     *
     * @param request 订单列表参数
     * @return 订单列表
     */
    Page<OrderWithProductDto> orderWithProduct(MallOrderListRequest request);

    /**
     * 获取过期订单
     *
     * @param expiredTime 过期时间点, 毫秒级时间戳, 小于该时间点的订单视为过期
     * @return 过期订单
     */
    List<MallOrder> getExpiredOrderClean(long expiredTime);

    /**
     * 获取用户订单
     *
     * @param userId  用户ID
     * @param request 查询参数
     * @return 用户订单
     */
    Page<MallOrder> getPaidOrderPage(Long userId, PageRequest request);

    /**
     * 获取用户订单统计信息
     *
     * @param userId 用户ID
     * @return 订单统计信息
     */
    UserOrderStatistics getOrderStatisticsByUserId(Long userId);
}
