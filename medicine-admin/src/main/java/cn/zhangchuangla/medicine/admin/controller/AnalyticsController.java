package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.vo.analytics.*;
import cn.zhangchuangla.medicine.admin.service.AnalyticsService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@Tag(name = "运营分析")
@IsAdmin
public class AnalyticsController extends BaseController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "运营总览")
    public AjaxResult<OverviewVo> overview() {
        return success(analyticsService.overview());
    }

    @GetMapping("/order/trend")
    @Operation(summary = "订单趋势", description = "period 支持 DAY/WEEK/MONTH")
    public AjaxResult<List<OrderTrendPoint>> orderTrend(@RequestParam(defaultValue = "DAY") String period) {
        return success(analyticsService.orderTrend(period));
    }

    @GetMapping("/order/status-distribution")
    @Operation(summary = "订单状态分布")
    public AjaxResult<List<StatusDistribution>> orderStatusDistribution() {
        return success(analyticsService.orderStatusDistribution());
    }

    @GetMapping("/order/payment-distribution")
    @Operation(summary = "支付方式分布")
    public AjaxResult<List<PaymentDistribution>> paymentDistribution() {
        return success(analyticsService.paymentDistribution());
    }

    @GetMapping("/product/hot")
    @Operation(summary = "热销商品排行榜")
    public AjaxResult<List<HotProductRank>> hotProducts(@RequestParam(defaultValue = "10") int limit) {
        return success(analyticsService.hotProducts(limit));
    }

    @GetMapping("/product/return-rate")
    @Operation(summary = "商品退货率")
    public AjaxResult<List<ReturnRateStat>> returnRates(@RequestParam(defaultValue = "10") int limit) {
        return success(analyticsService.productReturnRates(limit));
    }
}
