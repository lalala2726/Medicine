package cn.zhangchuangla.medicine.agent.controller.admin;

import cn.zhangchuangla.medicine.agent.config.condition.ConditionalOnAgentSpi;
import cn.zhangchuangla.medicine.agent.model.vo.analytics.*;
import cn.zhangchuangla.medicine.agent.spi.AdminAnalyticsDataProvider;
import cn.zhangchuangla.medicine.agent.spi.AgentSpiLoader;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin 端智能体运营分析工具接口。
 */
@RestController
@RequestMapping("/agent/tools/analytics")
@Tag(name = "Admin智能体运营分析工具", description = "用于 Admin 侧智能体运营分析查询接口")
@ConditionalOnAgentSpi(AdminAnalyticsDataProvider.class)
public class AdminAgentAnalyticsController extends BaseController {

    /**
     * 获取运营总览数据。
     */
    @GetMapping("/overview")
    @Operation(summary = "运营总览", description = "包括总订单数、总销售额、总用户数等关键指标")
    public AjaxResult<OverviewVo> overview() {
        AdminAnalyticsDataProvider provider = AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class);
        return success(provider.overview());
    }

    /**
     * 获取订单趋势数据。
     */
    @GetMapping("/order/trend")
    @Operation(summary = "订单趋势", description = "根据指定的时间周期统计订单数量和金额的变化趋势，period 支持 DAY/WEEK/MONTH")
    public AjaxResult<List<OrderTrendPoint>> orderTrend(
            @Parameter(description = "时间周期，支持 DAY(日)、WEEK(周)、MONTH(月)")
            @RequestParam(defaultValue = "DAY") String period
    ) {
        AdminAnalyticsDataProvider provider = AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class);
        return success(provider.orderTrend(period));
    }

    /**
     * 获取订单状态分布。
     */
    @GetMapping("/order/status-distribution")
    @Operation(summary = "订单状态分布", description = "统计不同状态订单的数量和占比")
    public AjaxResult<List<StatusDistribution>> orderStatusDistribution() {
        AdminAnalyticsDataProvider provider = AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class);
        return success(provider.orderStatusDistribution());
    }

    /**
     * 获取支付方式分布。
     */
    @GetMapping("/order/payment-distribution")
    @Operation(summary = "支付方式分布", description = "统计不同支付方式的使用情况和占比")
    public AjaxResult<List<PaymentDistribution>> paymentDistribution() {
        AdminAnalyticsDataProvider provider = AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class);
        return success(provider.paymentDistribution());
    }

    /**
     * 获取热销商品排行榜。
     */
    @GetMapping("/product/hot")
    @Operation(summary = "热销商品排行榜", description = "根据销量统计最受欢迎的商品")
    public AjaxResult<List<HotProductRank>> hotProducts(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit
    ) {
        AdminAnalyticsDataProvider provider = AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class);
        return success(provider.hotProducts(limit));
    }

    /**
     * 获取商品退货率统计。
     */
    @GetMapping("/product/return-rate")
    @Operation(summary = "商品退货率", description = "统计商品的退货情况，帮助发现质量问题")
    public AjaxResult<List<ReturnRateStat>> returnRates(
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") int limit
    ) {
        AdminAnalyticsDataProvider provider = AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class);
        return success(provider.productReturnRates(limit));
    }
}
