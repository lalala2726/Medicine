package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.model.vo.admin.PaymentDistribution;
import cn.zhangchuangla.medicine.agent.model.vo.admin.StatusDistribution;
import cn.zhangchuangla.medicine.agent.service.AnalyticsService;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.model.vo.analytics.HotProductRank;
import cn.zhangchuangla.medicine.model.vo.analytics.OrderTrendPoint;
import cn.zhangchuangla.medicine.model.vo.analytics.OverviewVo;
import cn.zhangchuangla.medicine.model.vo.analytics.ReturnRateStat;
import cn.zhangchuangla.medicine.rpc.admin.AdminAgentAnalyticsRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 运营分析 Dubbo Consumer 实现。
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @DubboReference(group = "medicine-admin", version = "1.0.0", check = false, timeout = 3000, retries = 0,
            url = "${dubbo.references.medicine-admin.url:}")
    private AdminAgentAnalyticsRpcService adminAgentAnalyticsRpcService;

    @Override
    public OverviewVo overview() {
        return adminAgentAnalyticsRpcService.overview();
    }

    @Override
    public List<OrderTrendPoint> orderTrend(String period) {
        return adminAgentAnalyticsRpcService.orderTrend(period);
    }

    @Override
    public List<StatusDistribution> orderStatusDistribution() {
        return BeanCotyUtils.copyListProperties(adminAgentAnalyticsRpcService.orderStatusDistribution(), StatusDistribution.class);
    }

    @Override
    public List<PaymentDistribution> paymentDistribution() {
        return BeanCotyUtils.copyListProperties(adminAgentAnalyticsRpcService.paymentDistribution(), PaymentDistribution.class);
    }

    @Override
    public List<HotProductRank> hotProducts(int limit) {
        return adminAgentAnalyticsRpcService.hotProducts(limit);
    }

    @Override
    public List<ReturnRateStat> productReturnRates(int limit) {
        return adminAgentAnalyticsRpcService.productReturnRates(limit);
    }
}
