package cn.zhangchuangla.medicine.dubbo.api.admin;

import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.dubbo.api.model.AdminOrderListQuery;
import cn.zhangchuangla.medicine.dubbo.api.model.AgentOrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;

import java.util.List;

/**
 * 管理端 Agent 订单只读 RPC。
 */
public interface AdminAgentOrderRpcService {

    PageResult<OrderWithProductDto> listOrders(AdminOrderListQuery query);

    List<AgentOrderDetailDto> getOrderDetailsByOrderNos(List<String> orderNos);
}
