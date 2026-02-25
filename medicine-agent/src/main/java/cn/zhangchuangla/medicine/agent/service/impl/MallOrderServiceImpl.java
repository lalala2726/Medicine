package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallOrderService;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.dto.OrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentOrderRpcService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 订单服务 Dubbo Consumer 实现。
 */
@Service
public class MallOrderServiceImpl implements MallOrderService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 30000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentOrderRpcService adminAgentOrderRpcService;

    @Override
    public Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request) {
        AdminMallOrderListRequest safeRequest = request == null ? new AdminMallOrderListRequest() : request;
        MallOrderListRequest query = BeanCotyUtils.copyProperties(safeRequest, MallOrderListRequest.class);
        PageResult<OrderWithProductDto> result = adminAgentOrderRpcService.listOrders(query);
        return toPage(result);
    }

    @Override
    public List<AdminOrderDetailVo> getOrderDetail(List<String> orderNos) {
        List<OrderDetailDto> details = adminAgentOrderRpcService.getOrderDetailsByOrderNos(orderNos);
        return details.stream().map(this::toAdminOrderDetail).toList();
    }

    private Page<OrderWithProductDto> toPage(PageResult<OrderWithProductDto> result) {
        if (result == null) {
            return new Page<>(1, 10, 0);
        }
        long pageNum = result.getPageNum() == null ? 1L : result.getPageNum();
        long pageSize = result.getPageSize() == null ? 10L : result.getPageSize();
        long total = result.getTotal() == null ? 0L : result.getTotal();
        Page<OrderWithProductDto> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(result.getRows() == null ? List.of() : result.getRows());
        return page;
    }

    private AdminOrderDetailVo toAdminOrderDetail(OrderDetailDto source) {
        if (source == null) {
            return null;
        }
        AdminOrderDetailVo target = new AdminOrderDetailVo();
        target.setUserInfo(BeanCotyUtils.copyProperties(source.getUserInfo(), AdminOrderDetailVo.UserInfo.class));
        target.setDeliveryInfo(BeanCotyUtils.copyProperties(source.getDeliveryInfo(), AdminOrderDetailVo.DeliveryInfo.class));
        target.setOrderInfo(BeanCotyUtils.copyProperties(source.getOrderInfo(), AdminOrderDetailVo.OrderInfo.class));
        target.setProductInfo(BeanCotyUtils.copyListProperties(source.getProductInfo(), AdminOrderDetailVo.ProductInfo.class));
        return target;
    }
}
