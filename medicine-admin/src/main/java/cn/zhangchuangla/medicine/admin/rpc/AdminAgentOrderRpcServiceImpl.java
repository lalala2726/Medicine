package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.admin.service.MallOrderTimelineService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.dto.OrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrderTimeline;
import cn.zhangchuangla.medicine.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.model.vo.MallOrderTimelineVo;
import cn.zhangchuangla.medicine.model.vo.OrderShippingVo;
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
    private final MallOrderTimelineService mallOrderTimelineService;

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

    @Override
    public List<MallOrderTimelineVo> getOrderTimeline(Long orderId) {
        List<MallOrderTimeline> timeline = mallOrderTimelineService.getTimelineByOrderId(orderId);
        return BeanCotyUtils.copyListProperties(timeline, MallOrderTimelineVo.class);
    }

    @Override
    public OrderShippingVo getOrderShipping(Long orderId) {
        return mallOrderService.getOrderShipping(orderId);
    }
}
