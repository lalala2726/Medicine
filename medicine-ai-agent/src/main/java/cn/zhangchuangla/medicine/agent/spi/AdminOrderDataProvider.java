package cn.zhangchuangla.medicine.agent.spi;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Admin 端智能体订单数据提供者。
 */
public interface AdminOrderDataProvider {

    /**
     * 分页查询订单列表。
     */
    Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request);

    /**
     * 批量查询订单详情。
     */
    List<AdminOrderDetailVo> getOrderDetail(List<Long> orderIds);
}
