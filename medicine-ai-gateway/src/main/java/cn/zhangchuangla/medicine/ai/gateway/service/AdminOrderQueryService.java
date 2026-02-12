package cn.zhangchuangla.medicine.ai.gateway.service;

import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLOrderQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AI 订单查询服务接口
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
public interface AdminOrderQueryService {

    /**
     * 分页查询订单列表。
     *
     * @return 分页结果
     */
    Page<MallOrder> searchOrders(GraphQLOrderQuery query);

    /**
     * 根据订单ID查询订单详情。
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    MallOrder getOrderById(Long orderId);
}
