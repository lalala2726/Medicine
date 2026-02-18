package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminMallOrderListVo;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminMallOrderProductInfoVo;
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
 * Admin 端智能体订单工具控制器。
 * <p>
 * 提供给管理端智能体使用的订单查询工具接口，
 * 支持订单列表查询和订单详情查询等功能。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/order")
@Tag(name = "Admin智能体订单工具", description = "用于 Admin 侧智能体订单查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
public class AdminAgentOrderToolsController extends BaseController {

    private final MallOrderService agentOrderService;

    /**
     * 根据条件分页查询订单列表。
     * <p>
     * 支持按订单状态、时间范围等条件筛选订单，
     * 返回订单基本信息及首个商品信息，按创建时间倒序排列。
     *
     * @param request 查询请求参数
     * @return 订单列表分页数据
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
     * 根据订单 ID 批量查询订单详情。
     * <p>
     * 返回订单的详细信息，包括订单基本信息、收货地址、
     * 商品明细、支付信息等完整订单数据。
     *
     * @param orderIds 订单 ID 列表，支持批量查询
     * @return 订单详情列表
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
        if (source == null) {
            return null;
        }
        AdminMallOrderListVo target = new AdminMallOrderListVo();
        target.setId(source.getId());
        target.setOrderNo(source.getOrderNo());
        target.setTotalAmount(source.getTotalAmount());
        target.setPayType(source.getPayType());
        target.setOrderStatus(source.getOrderStatus());
        target.setPayTime(source.getPayTime());
        target.setCreateTime(source.getCreateTime());

        if (source.getProductId() == null) {
            return target;
        }

        AdminMallOrderProductInfoVo productInfo = new AdminMallOrderProductInfoVo();
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
