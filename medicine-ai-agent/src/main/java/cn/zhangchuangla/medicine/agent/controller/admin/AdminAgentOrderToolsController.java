package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminMallOrderListVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallOrderService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin 端智能体订单工具接口。
 */
@RestController
@RequestMapping("/agent/order")
@Tag(name = "Admin智能体订单工具", description = "用于 Admin 侧智能体订单查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AdminAgentOrderToolsController extends BaseController {

    private final MallOrderService agentOrderService;

    /**
     * 获取订单列表。
     */
    @GetMapping("/list")
    @Operation(summary = "获取订单列表", description = "分页获取订单列表，默认按创建时间倒序")
    @PreAuthorize("hasAuthority('mall:order:list') or hasRole('super_admin')")
    public AjaxResult<TableDataResult> getOrderList(AdminMallOrderListRequest request) {
        AdminMallOrderListRequest safeRequest = request == null ? new AdminMallOrderListRequest() : request;
        Page<OrderWithProductDto> orderPage = agentOrderService.listOrders(safeRequest);
        List<AdminMallOrderListVo> orderListVos = orderPage.getRecords().stream()
                .map(this::buildOrderListVo)
                .toList();
        return getTableData(orderPage, orderListVos);
    }

    /**
     * 获取订单详情。
     */
    @GetMapping("/{orderIds}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取详细信息")
    @PreAuthorize("hasAuthority('mall:order:query') or hasRole('super_admin')")
    public AjaxResult<List<AdminOrderDetailVo>> getOrderDetail(
            @Parameter(description = "订单ID")
            @PathVariable List<Long> orderIds
    ) {
        return success(agentOrderService.getOrderDetail(orderIds));
    }

    private AdminMallOrderListVo buildOrderListVo(OrderWithProductDto source) {
        AdminMallOrderListVo target = copyProperties(source, AdminMallOrderListVo.class);
        if (target == null) {
            return null;
        }
        if (source.getProductId() == null) {
            return target;
        }
        AdminMallOrderListVo.ProductInfo productInfo = new AdminMallOrderListVo.ProductInfo();
        productInfo.setProductName(source.getProductName());
        productInfo.setProductImage(source.getProductImage());
        productInfo.setProductPrice(source.getProductPrice());
        productInfo.setProductCategory(source.getProductCategory());
        productInfo.setProductId(source.getProductId());
        productInfo.setQuantity(source.getProductQuantity());
        target.setProductInfo(productInfo);
        return target;
    }
}
