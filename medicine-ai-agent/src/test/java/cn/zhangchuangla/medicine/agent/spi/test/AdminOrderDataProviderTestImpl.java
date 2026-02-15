package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.spi.AdminOrderDataProvider;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * AdminOrderDataProvider 测试实现。
 */
public class AdminOrderDataProviderTestImpl implements AdminOrderDataProvider {

    @Override
    public Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request) {
        TestAgentSpiData.capturedOrderListRequest = request;
        return TestAgentSpiData.orderPage;
    }

    @Override
    public List<AdminOrderDetailVo> getOrderDetail(List<Long> orderIds) {
        TestAgentSpiData.capturedOrderDetailIds = orderIds;
        return TestAgentSpiData.orderDetails;
    }
}
