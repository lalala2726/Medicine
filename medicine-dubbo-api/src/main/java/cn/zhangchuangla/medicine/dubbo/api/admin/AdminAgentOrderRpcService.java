package cn.zhangchuangla.medicine.dubbo.api.admin;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.OrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.request.MallOrderListRequest;

import java.util.List;

/**
 * 管理端 Agent 订单只读 RPC。
 */
public interface AdminAgentOrderRpcService {

    /**
     * 分页查询订单及关联商品信息。
     *
     * @param query 订单查询参数
     * @return 分页订单结果
     */
    PageResult<OrderWithProductDto> listOrders(MallOrderListRequest query);

    /**
     * 根据订单编号列表批量查询订单详情。
     *
     * @param orderNos 订单编号列表
     * @return 订单详情列表
     */
    List<OrderDetailDto> getOrderDetailsByOrderNos(List<String> orderNos);
}
