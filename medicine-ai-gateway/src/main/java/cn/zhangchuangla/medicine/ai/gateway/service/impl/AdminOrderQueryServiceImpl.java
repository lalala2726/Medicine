package cn.zhangchuangla.medicine.ai.gateway.service.impl;

import cn.zhangchuangla.medicine.ai.gateway.mapper.AdminOrderMapper;
import cn.zhangchuangla.medicine.ai.gateway.service.AdminOrderQueryService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.request.graphql.GraphQLOrderQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 订单查询服务实现
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@Service
public class AdminOrderQueryServiceImpl implements AdminOrderQueryService {

    private final AdminOrderMapper adminOrderMapper;

    public AdminOrderQueryServiceImpl(AdminOrderMapper adminOrderMapper) {
        this.adminOrderMapper = adminOrderMapper;
    }

    @Override
    public Page<MallOrder> searchOrders(GraphQLOrderQuery query) {
        GraphQLOrderQuery safeQuery = query == null ? new GraphQLOrderQuery() : query;
        safeQuery.setOrderNo(StringUtils.trimToNull(safeQuery.getOrderNo()));
        safeQuery.setPayType(StringUtils.trimToNull(safeQuery.getPayType()));
        safeQuery.setOrderStatus(StringUtils.trimToNull(safeQuery.getOrderStatus()));
        safeQuery.setDeliveryType(StringUtils.trimToNull(safeQuery.getDeliveryType()));
        safeQuery.setReceiverName(StringUtils.trimToNull(safeQuery.getReceiverName()));
        safeQuery.setReceiverPhone(StringUtils.trimToNull(safeQuery.getReceiverPhone()));

        Page<MallOrder> page = adminOrderMapper.listOrderPage(safeQuery.toPage(), safeQuery);
        if (page.getRecords() == null) {
            page.setRecords(List.of());
        }
        return page;
    }

    @Override
    public MallOrder getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new ServiceException("订单ID不能为空");
        }
        MallOrder order = adminOrderMapper.getOrderById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        return order;
    }
}
