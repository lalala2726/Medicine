package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.model.vo.analytics.*;

import java.util.List;

public interface AnalyticsService {

    OverviewVo overview();

    List<OrderTrendPoint> orderTrend(String period);

    List<StatusDistribution> orderStatusDistribution();

    List<PaymentDistribution> paymentDistribution();

    List<HotProductRank> hotProducts(int limit);

    List<ReturnRateStat> productReturnRates(int limit);
}
