package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.vo.analytics.*;
import cn.zhangchuangla.medicine.admin.service.AnalyticsService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运营分析控制器
 * 提供运营数据分析相关的接口，包括订单趋势、商品销售等统计数据
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/analytics")
@Tag(name = "运营分析")
@PreAuthorize("hasRole('super_admin')")
public class AnalyticsController extends BaseController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * 获取运营总览数据
     * 包括总订单数、总销售额、总用户数等关键指标
     *
     * @return 运营总览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "运营总览")
    public AjaxResult<OverviewVo> overview() {
        return success(analyticsService.overview());
    }

    /**
     * 获取订单趋势数据
     * 根据指定的时间周期统计订单数量和金额的变化趋势
     *
     * @param period 时间周期，支持 DAY(日)、WEEK(周)、MONTH(月)，默认为 DAY
     * @return 订单趋势数据点列表
     */
    @GetMapping("/order/trend")
    @Operation(summary = "订单趋势", description = "period 支持 DAY/WEEK/MONTH")
    public AjaxResult<List<OrderTrendPoint>> orderTrend(@RequestParam(defaultValue = "DAY") String period) {
        return success(analyticsService.orderTrend(period));
    }

    /**
     * 获取订单状态分布
     * 统计不同状态订单的数量和占比
     *
     * @return 订单状态分布数据列表
     */
    @GetMapping("/order/status-distribution")
    @Operation(summary = "订单状态分布")
    public AjaxResult<List<StatusDistribution>> orderStatusDistribution() {
        return success(analyticsService.orderStatusDistribution());
    }

    /**
     * 获取支付方式分布
     * 统计不同支付方式的使用情况和占比
     *
     * @return 支付方式分布数据列表
     */
    @GetMapping("/order/payment-distribution")
    @Operation(summary = "支付方式分布")
    public AjaxResult<List<PaymentDistribution>> paymentDistribution() {
        return success(analyticsService.paymentDistribution());
    }

    /**
     * 获取热销商品排行榜
     * 根据销量统计最受欢迎的商品
     *
     * @param limit 返回数量限制，默认为 10
     * @return 热销商品排行榜数据列表
     */
    @GetMapping("/product/hot")
    @Operation(summary = "热销商品排行榜")
    public AjaxResult<List<HotProductRank>> hotProducts(@RequestParam(defaultValue = "10") int limit) {
        return success(analyticsService.hotProducts(limit));
    }

    /**
     * 获取商品退货率统计
     * 统计商品的退货情况，帮助发现质量问题
     *
     * @param limit 返回数量限制，默认为 10
     * @return 商品退货率统计数据列表
     */
    @GetMapping("/product/return-rate")
    @Operation(summary = "商品退货率")
    public AjaxResult<List<ReturnRateStat>> returnRates(@RequestParam(defaultValue = "10") int limit) {
        return success(analyticsService.productReturnRates(limit));
    }
}
