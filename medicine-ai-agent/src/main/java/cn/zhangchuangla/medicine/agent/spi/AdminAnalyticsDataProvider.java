package cn.zhangchuangla.medicine.agent.spi;

import cn.zhangchuangla.medicine.agent.model.vo.analytics.*;

import java.util.List;

/**
 * Admin 端智能体运营分析数据提供者。
 */
public interface AdminAnalyticsDataProvider {

    /**
     * 获取运营总览数据。
     *
     * @return 运营总览数据
     */
    OverviewVo overview();

    /**
     * 获取订单趋势数据。
     *
     * @param period 时间周期，支持 DAY(日)、WEEK(周)、MONTH(月)
     * @return 订单趋势数据点列表
     */
    List<OrderTrendPoint> orderTrend(String period);

    /**
     * 获取订单状态分布。
     *
     * @return 订单状态分布数据列表
     */
    List<StatusDistribution> orderStatusDistribution();

    /**
     * 获取支付方式分布。
     *
     * @return 支付方式分布数据列表
     */
    List<PaymentDistribution> paymentDistribution();

    /**
     * 获取热销商品排行榜。
     *
     * @param limit 返回数量限制
     * @return 热销商品排行榜数据列表
     */
    List<HotProductRank> hotProducts(int limit);

    /**
     * 获取商品退货率统计。
     *
     * @param limit 返回数量限制
     * @return 商品退货率统计数据列表
     */
    List<ReturnRateStat> productReturnRates(int limit);
}