package cn.zhangchuangla.medicine.admin.controller.mall;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.MallOrderListVo;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 21:07
 */
@RestController
@RequestMapping("/mall/order")
@Tag(name = "订单管理", description = "订单管理")
@IsAdmin
public class MallOrderController extends BaseController {

    private final MallOrderService mallOrderService;


    public MallOrderController(MallOrderService mallOrderService) {
        this.mallOrderService = mallOrderService;
    }


    /**
     * 订单列表
     *
     * @param request 查询参数
     * @return 订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public AjaxResult<List<MallOrderListVo>> orderList(MallOrderListRequest request) {
        Page<MallOrder> mallOrderPage = mallOrderService.orderList(request);
        List<MallOrderListVo> mallOrderListVos = BeanCotyUtils.copyListProperties(mallOrderPage, MallOrderListVo.class);
        return success(mallOrderListVos);
    }

    /**
     * 订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "订单详情")
    public AjaxResult<OrderDetailVo> orderDetail(@PathVariable("orderId") Long orderId) {
        OrderDetailVo orderDetailVo = mallOrderService.orderDetail(orderId);
        return success(orderDetailVo);
    }

    /**
     * 修改订单地址
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping("/address")
    @Operation(summary = "修改订单地址")
    public AjaxResult<?> updateOrderAddress(@Validated @RequestBody AddressUpdateRequest request) {
        boolean result = mallOrderService.updateOrderAddress(request);
        return toAjax(result);
    }

    /**
     * 修改订单备注
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping("/remark")
    @Operation(summary = "修改订单备注")
    public AjaxResult<?> updateOrderRemark(@Validated @RequestBody RemarkUpdateRequest request) {
        boolean result = mallOrderService.updateOrderRemark(request);
        return toAjax(result);
    }

    /**
     * 订单改价
     *
     * @param request 订单改价参数
     * @return 订单改价结果
     */
    @PutMapping("/price")
    @Operation(summary = "订单改价")
    public AjaxResult<?> updateOrderPrice(@Validated @RequestBody OrderUpdatePriceRequest request) {
        boolean result = mallOrderService.updateOrderPrice(request);
        return toAjax(result);
    }


    /**
     * 订单退款
     *
     * @param request 订单退款参数
     * @return 订单退款结果
     */
    @PostMapping
    @Operation(summary = "订单退款")
    public AjaxResult<?> OrderRefund(OrderRefundRequest request) {
        boolean result = mallOrderService.orderRefund(request);
        return toAjax(result);
    }


}
