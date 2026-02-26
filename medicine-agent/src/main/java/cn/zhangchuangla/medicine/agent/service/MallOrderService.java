package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.OrderDetailVo;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.vo.MallOrderTimelineVo;
import cn.zhangchuangla.medicine.model.vo.OrderShippingVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 智能体订单服务接口。
 * <p>
 * 提供订单相关的查询服务，包括订单列表查询和订单详情查询。
 *
 * @author Chuang
 */
public interface MallOrderService {

    /**
     * 分页查询订单列表。
     *
     * @param request 查询请求参数，包含订单状态、时间范围等筛选条件
     * @return 订单与商品信息分页数据
     */
    Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request);

    /**
     * 根据订单编号批量查询订单详细信息。
     *
     * @param orderNos 订单编号列表
     * @return 订单详情列表
     */
    List<OrderDetailVo> getOrderDetail(List<String> orderNos);

    /**
     * 根据订单ID查询订单流程（时间线）。
     *
     * @param orderId 订单ID
     * @return 订单流程列表
     */
    List<MallOrderTimelineVo> getOrderTimeline(Long orderId);

    /**
     * 根据订单ID查询发货记录。
     *
     * @param orderId 订单ID
     * @return 发货记录
     */
    OrderShippingVo getOrderShipping(Long orderId);
}
