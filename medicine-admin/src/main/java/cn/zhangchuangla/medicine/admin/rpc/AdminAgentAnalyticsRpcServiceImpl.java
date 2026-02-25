package cn.zhangchuangla.medicine.admin.rpc;

import cn.zhangchuangla.medicine.admin.service.AnalyticsService;
import cn.zhangchuangla.medicine.dubbo.api.admin.AdminAgentAnalyticsRpcService;
import cn.zhangchuangla.medicine.model.vo.analytics.*;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 管理端 Agent 运营分析 RPC Provider。
 */
@DubboService(interfaceClass = AdminAgentAnalyticsRpcService.class, group = "medicine-admin", version = "1.0.0")
@RequiredArgsConstructor
public class AdminAgentAnalyticsRpcServiceImpl implements AdminAgentAnalyticsRpcService {

    private final AnalyticsService analyticsService;

    @Override
    public OverviewVo overview() {
        return analyticsService.overview();
    }

    @Override
    public List<OrderTrendPoint> orderTrend(String period) {
        return analyticsService.orderTrend(period);
    }

    @Override
    public List<StatusDistribution> orderStatusDistribution() {
        return analyticsService.orderStatusDistribution();
    }

    @Override
    public List<PaymentDistribution> paymentDistribution() {
        return analyticsService.paymentDistribution();
    }

    @Override
    public List<HotProductRank> hotProducts(int limit) {
        return analyticsService.hotProducts(limit);
    }

    @Override
    public List<ReturnRateStat> productReturnRates(int limit) {
        return analyticsService.productReturnRates(limit);
    }
}
