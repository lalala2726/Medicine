package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentOrderRpcService;
import cn.zhangchuangla.medicine.dubbo.api.model.AdminOrderListQuery;
import cn.zhangchuangla.medicine.dubbo.api.model.AgentOrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 管理端 Agent 订单 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentOrderRpcService.class, group = "medicine-admin", version = "1.0.0", timeout = 30000)
@RequiredArgsConstructor
public class AdminAgentOrderRpcServiceImpl implements AdminAgentOrderRpcService {

    private final MallOrderService mallOrderService;

    @Override
    public PageResult<OrderWithProductDto> listOrders(AdminOrderListQuery query) {
        MallOrderListRequest request = query == null ? new MallOrderListRequest()
                : BeanCotyUtils.copyProperties(query, MallOrderListRequest.class);
        Page<OrderWithProductDto> page = mallOrderService.orderWithProduct(request);
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public List<AgentOrderDetailDto> getOrderDetailsByOrderNos(List<String> orderNos) {
        return mallOrderService.getOrderByOrderNo(orderNos).stream()
                .map(this::toAgentOrderDetail)
                .toList();
    }

    private AgentOrderDetailDto toAgentOrderDetail(OrderDetailVo source) {
        if (source == null) {
            return null;
        }
        AgentOrderDetailDto target = new AgentOrderDetailDto();

        AgentOrderDetailDto.UserInfo userInfo = BeanCotyUtils.copyProperties(source.getUserInfo(), AgentOrderDetailDto.UserInfo.class);
        target.setUserInfo(userInfo);

        AgentOrderDetailDto.DeliveryInfo deliveryInfo = BeanCotyUtils.copyProperties(source.getDeliveryInfo(), AgentOrderDetailDto.DeliveryInfo.class);
        target.setDeliveryInfo(deliveryInfo);

        AgentOrderDetailDto.OrderInfo orderInfo = BeanCotyUtils.copyProperties(source.getOrderInfo(), AgentOrderDetailDto.OrderInfo.class);
        target.setOrderInfo(orderInfo);

        List<AgentOrderDetailDto.ProductInfo> products = BeanCotyUtils.copyListProperties(source.getProductInfo(), AgentOrderDetailDto.ProductInfo.class);
        target.setProductInfo(products);
        return target;
    }
}
