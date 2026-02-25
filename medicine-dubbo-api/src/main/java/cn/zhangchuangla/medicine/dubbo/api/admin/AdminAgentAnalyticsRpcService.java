package cn.zhangchuangla.medicine.dubbo.api.admin;

import cn.zhangchuangla.medicine.model.vo.analytics.*;

import java.util.List;

/**
 * 管理端 Agent 运营分析只读 RPC。
 */
public interface AdminAgentAnalyticsRpcService {

    OverviewVo overview();

    List<OrderTrendPoint> orderTrend(String period);

    List<StatusDistribution> orderStatusDistribution();

    List<PaymentDistribution> paymentDistribution();

    List<HotProductRank> hotProducts(int limit);

    List<ReturnRateStat> productReturnRates(int limit);
}
