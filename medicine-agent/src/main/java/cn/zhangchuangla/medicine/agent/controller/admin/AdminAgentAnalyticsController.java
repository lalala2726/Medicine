package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.annotation.InternalAgentHeaderTrace;
import cn.zhangchuangla.medicine.agent.model.vo.analytics.PaymentDistribution;
import cn.zhangchuangla.medicine.agent.model.vo.analytics.StatusDistribution;
import cn.zhangchuangla.medicine.agent.service.AnalyticsService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.vo.analytics.HotProductRank;
import cn.zhangchuangla.medicine.model.vo.analytics.OrderTrendPoint;
import cn.zhangchuangla.medicine.model.vo.analytics.OverviewVo;
import cn.zhangchuangla.medicine.model.vo.analytics.ReturnRateStat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端智能体运营分析工具控制器。
 * <p>
 * 提供给管理端智能体使用的运营数据分析工具接口，
 * 包括订单统计、销售趋势、商品排行等数据分析功能。
 * 仅超级管理员可访问。
 *
 * @author Chuang
 */
@RestController
@RequestMapping("/agent/analytics")
@Tag(name = "管理端智能体运营分析工具", description = "用于管理端智能体运营分析查询接口")
@InternalAgentHeaderTrace
@RequiredArgsConstructor
@PreAuthorize("hasRole('super_admin')")
public class AdminAgentAnalyticsController extends BaseController {

    private final AnalyticsService agentAnalyticsService;

    /**
     * 获取运营总览数据。
     * <p>
     * 返回平台的关键运营指标，包括总订单数、总销售额、
     * 总用户数、退款统计等核心数据。
     *
     * @return 运营总览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "运营总览", description = "包括总订单数、总销售额、总用户数等关键指标")
    public AjaxResult<OverviewVo> overview() {
        return success(agentAnalyticsService.overview());
    }

    /**
     * 获取订单趋势数据。
     * <p>
     * 根据指定的时间周期统计订单数量和金额的变化趋势，
     * 支持按日(DAY)、周(WEEK)、月(MONTH)三种周期统计。
     *
     * @param period 时间周期，支持 DAY(日)、WEEK(周)、MONTH(月)
     * @return 订单趋势数据点列表
     */
    @GetMapping("/order/trend")
    @Operation(summary = "订单趋势", description = "根据指定的时间周期统计订单数量和金额的变化趋势，period 支持 DAY/WEEK/MONTH")
    public AjaxResult<List<OrderTrendPoint>> orderTrend(
            @Parameter(description = "时间周期，支持 DAY(日)、WEEK(周)、MONTH(月)")
            @RequestParam(defaultValue = "DAY") String period
    ) {
        return success(agentAnalyticsService.orderTrend(period));
    }

    /**
     * 获取订单状态分布。
     * <p>
     * 统计不同状态（待付款、待发货、待收货、已完成、已取消等）
     * 的订单数量及其占比。
     *
     * @return 订单状态分布列表
     */
    @GetMapping("/order/status-distribution")
    @Operation(summary = "订单状态分布", description = "统计不同状态订单的数量和占比")
    public AjaxResult<List<StatusDistribution>> orderStatusDistribution() {
        return success(agentAnalyticsService.orderStatusDistribution());
    }

    /**
     * 获取支付方式分布。
     * <p>
     * 统计不同支付方式（支付宝、微信等）的使用次数及其占比。
     *
     * @return 支付方式分布列表
     */
    @GetMapping("/order/payment-distribution")
    @Operation(summary = "支付方式分布", description = "统计不同支付方式的使用情况和占比")
    public AjaxResult<List<PaymentDistribution>> paymentDistribution() {
        return success(agentAnalyticsService.paymentDistribution());
    }

    /**
     * 获取热销商品排行榜。
     * <p>
     * 根据已完成订单的销量统计最受欢迎的商品，
     * 按销量降序排列。
     *
     * @param limit 返回数量限制，默认 10 条
     * @return 热销商品排行榜
     */
    @GetMapping("/product/hot")
    @Operation(summary = "热销商品排行榜", description = "根据销量统计最受欢迎的商品")
    public AjaxResult<List<HotProductRank>> hotProducts(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit
    ) {
        return success(agentAnalyticsService.hotProducts(limit));
    }

    /**
     * 获取商品退货率统计。
     * <p>
     * 统计商品的退货情况，帮助发现潜在的质量问题，
     * 按退货率降序排列。
     *
     * @param limit 返回数量限制，默认 10 条
     * @return 商品退货率统计列表
     */
    @GetMapping("/product/return-rate")
    @Operation(summary = "商品退货率", description = "统计商品的退货情况，帮助发现质量问题")
    public AjaxResult<List<ReturnRateStat>> returnRates(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit
    ) {
        return success(agentAnalyticsService.productReturnRates(limit));
    }
}
