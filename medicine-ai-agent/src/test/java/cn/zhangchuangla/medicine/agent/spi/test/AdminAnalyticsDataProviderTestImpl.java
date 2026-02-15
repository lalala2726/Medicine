package cn.zhangchuangla.medicine.agent.spi.test;

import cn.zhangchuangla.medicine.agent.model.vo.analytics.*;
import cn.zhangchuangla.medicine.agent.spi.AdminAnalyticsDataProvider;

import java.math.BigDecimal;
import java.util.List;

/**
 * AdminAnalyticsDataProvider 测试实现。
 */
public class AdminAnalyticsDataProviderTestImpl implements AdminAnalyticsDataProvider {

    @Override
    public OverviewVo overview() {
        TestAgentSpiData.capturedAnalyticsOverview = true;
        OverviewVo vo = new OverviewVo();
        vo.setTotalUsers(100L);
        vo.setTotalOrders(50L);
        vo.setPaidOrders(40L);
        vo.setRefundCount(5L);
        vo.setTotalAmount(new BigDecimal("5000.00"));
        vo.setAverageAmount(new BigDecimal("100.00"));
        vo.setRefundAmount(new BigDecimal("500.00"));
        return vo;
    }

    @Override
    public List<OrderTrendPoint> orderTrend(String period) {
        TestAgentSpiData.capturedAnalyticsPeriod = period;
        return TestAgentSpiData.orderTrendPoints;
    }

    @Override
    public List<StatusDistribution> orderStatusDistribution() {
        TestAgentSpiData.capturedAnalyticsStatusDistribution = true;
        return TestAgentSpiData.statusDistributions;
    }

    @Override
    public List<PaymentDistribution> paymentDistribution() {
        TestAgentSpiData.capturedAnalyticsPaymentDistribution = true;
        return TestAgentSpiData.paymentDistributions;
    }

    @Override
    public List<HotProductRank> hotProducts(int limit) {
        TestAgentSpiData.capturedAnalyticsHotProductsLimit = limit;
        return TestAgentSpiData.hotProductRanks;
    }

    @Override
    public List<ReturnRateStat> productReturnRates(int limit) {
        TestAgentSpiData.capturedAnalyticsReturnRatesLimit = limit;
        return TestAgentSpiData.returnRateStats;
    }
}
