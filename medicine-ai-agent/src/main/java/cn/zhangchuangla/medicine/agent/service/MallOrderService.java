package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface MallOrderService {

    Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request);

    List<AdminOrderDetailVo> getOrderDetail(List<Long> orderIds);
}
