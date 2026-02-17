package cn.zhangchuangla.medicine.agent.service;

import cn.zhangchuangla.medicine.model.vo.analytics.*;

import java.util.List;

public interface AnalyticsService {

    OverviewVo overview();

    List<OrderTrendPoint> orderTrend(String period);

    List<cn.zhangchuangla.medicine.model.vo.analytics.StatusDistribution> orderStatusDistribution();

    List<PaymentDistribution> paymentDistribution();

    List<HotProductRank> hotProducts(int limit);

    List<ReturnRateStat> productReturnRates(int limit);
}
