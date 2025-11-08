package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.*;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.admin.service.MallOrderTimelineService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrderTimeline;
import cn.zhangchuangla.medicine.model.vo.MallOrderTimelineVo;
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
    private final MallOrderTimelineService mallOrderTimelineService;


    public MallOrderController(MallOrderService mallOrderService, MallOrderTimelineService mallOrderTimelineService) {
        this.mallOrderService = mallOrderService;
        this.mallOrderTimelineService = mallOrderTimelineService;
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
        Page<OrderWithProductDto> mallOrderPage = mallOrderService.orderWithProduct(request);
        List<MallOrderListVo> mallOrderListVos = mallOrderPage.getRecords().stream()
                .map(this::buildOrderListVo)
                .toList();
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
     * 获取订单地址信息
     *
     * @param orderId 订单ID
     * @return 订单地址信息
     */
    @GetMapping("/address/{orderId}")
    @Operation(summary = "获取订单地址信息")
    public AjaxResult<OrderAddressVo> getOrderAddress(@PathVariable("orderId") Long orderId) {
        OrderAddressVo orderAddressVo = mallOrderService.getOrderAddress(orderId);
        return success(orderAddressVo);
    }

    /**
     * 修改订单地址
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping("/address")
    @Operation(summary = "修改订单地址")
    public AjaxResult<Void> updateOrderAddress(@Validated @RequestBody AddressUpdateRequest request) {
        boolean result = mallOrderService.updateOrderAddress(request);
        return toAjax(result);
    }

    /**
     * 获取订单备注信息
     *
     * @param orderId 订单ID
     * @return 订单备注信息
     */
    @GetMapping("/remark/{orderId}")
    @Operation(summary = "获取订单备注信息")
    public AjaxResult<OrderRemarkVo> getOrderRemark(@PathVariable("orderId") Long orderId) {
        OrderRemarkVo orderRemarkVo = mallOrderService.getOrderRemark(orderId);
        return success(orderRemarkVo);
    }

    /**
     * 修改订单备注
     *
     * @param request 修改参数
     * @return 修改结果
     */
    @PutMapping("/remark")
    @Operation(summary = "修改订单备注")
    public AjaxResult<Void> updateOrderRemark(@Validated @RequestBody RemarkUpdateRequest request) {
        boolean result = mallOrderService.updateOrderRemark(request);
        return toAjax(result);
    }

    /**
     * 获取订单价格信息
     *
     * @param orderId 订单ID
     * @return 订单价格信息
     */
    @GetMapping("/price/{orderId}")
    @Operation(summary = "获取订单价格信息")
    public AjaxResult<OrderPriceVo> getOrderPrice(@PathVariable("orderId") Long orderId) {
        OrderPriceVo orderPriceVo = mallOrderService.getOrderPrice(orderId);
        return success(orderPriceVo);
    }

    /**
     * 订单改价
     *
     * @param request 订单改价参数
     * @return 订单改价结果
     */
    @PutMapping("/price")
    @Operation(summary = "订单改价")
    public AjaxResult<Void> updateOrderPrice(@Validated @RequestBody OrderUpdatePriceRequest request) {
        boolean result = mallOrderService.updateOrderPrice(request);
        return toAjax(result);
    }


    /**
     * 订单退款
     *
     * @param request 订单退款参数
     * @return 订单退款结果
     */
    @PostMapping("/refund")
    @Operation(summary = "订单退款")
    public AjaxResult<Void> orderRefund(@RequestBody OrderRefundRequest request) {
        boolean result = mallOrderService.orderRefund(request);
        return toAjax(result);
    }

    /**
     * 取消订单
     * <p>
     * 如果订单已支付，会自动退款；如果未支付，直接取消并恢复库存
     * </p>
     *
     * @param request 订单取消参数
     * @return 取消结果
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消订单")
    public AjaxResult<Void> cancelOrder(@Validated @RequestBody OrderCancelRequest request) {
        boolean result = mallOrderService.cancelOrder(request);
        return toAjax(result);
    }

    /**
     * 查询订单时间线
     *
     * @param orderId 订单ID
     * @return 订单时间线列表
     */
    @GetMapping("/timeline/{orderId}")
    @Operation(summary = "查询订单时间线")
    public AjaxResult<List<MallOrderTimelineVo>> getOrderTimeline(@PathVariable("orderId") Long orderId) {
        List<MallOrderTimeline> timeline = mallOrderTimelineService.getTimelineByOrderId(orderId);
        List<MallOrderTimelineVo> mallOrderTimelineVos = copyListProperties(timeline, MallOrderTimelineVo.class);
        return success(mallOrderTimelineVos);
    }

    /**
     * 构建订单列表VO
     *
     * @param source 源数据
     * @return 订单列表VO
     */
    private MallOrderListVo buildOrderListVo(OrderWithProductDto source) {
        MallOrderListVo target = BeanCotyUtils.copyProperties(source, MallOrderListVo.class);
        if (target == null) {
            return null;
        }
        if (source.getProductId() == null) {
            return target;
        }
        MallOrderListVo.ProductInfo productInfo = MallOrderListVo.ProductInfo.builder()
                .productName(source.getProductName())
                .productImage(source.getProductImage())
                .quantity(source.getProductQuantity())
                .build();
        target.setProductInfo(productInfo);
        return target;
    }

}
