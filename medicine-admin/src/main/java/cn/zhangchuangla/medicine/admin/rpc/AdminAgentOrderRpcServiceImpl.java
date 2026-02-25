package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.dto.OrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentOrderRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 管理端 Agent 订单 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentOrderRpcService.class, group = "medicine-admin", version = "1.0.0", timeout = 10000)
@RequiredArgsConstructor
public class AdminAgentOrderRpcServiceImpl implements AdminAgentOrderRpcService {

    private final MallOrderService mallOrderService;

    @Override
    public PageResult<OrderWithProductDto> listOrders(MallOrderListRequest query) {
        MallOrderListRequest request = query == null ? new MallOrderListRequest() : query;
        Page<OrderWithProductDto> page = mallOrderService.orderWithProduct(request);
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public List<OrderDetailDto> getOrderDetailsByOrderNos(List<String> orderNos) {
        return mallOrderService.getOrderByOrderNo(orderNos);
    }
}
