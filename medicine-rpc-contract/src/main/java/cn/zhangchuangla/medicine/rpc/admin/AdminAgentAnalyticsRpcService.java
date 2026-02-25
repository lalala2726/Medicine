package cn.zhangchuangla.medicine.rpc.admin;

import cn.zhangchuangla.medicine.model.vo.analytics.*;

import java.util.List;

/**
 * 管理端 Agent 运营分析只读 RPC。
 */
public interface AdminAgentAnalyticsRpcService {

    /**
     * 获取管理端运营概览数据。
     *
     * @return 概览统计信息
     */
    OverviewVo overview();

    /**
     * 获取订单趋势数据。
     *
     * @param period 统计周期标识
     * @return 趋势点列表
     */
    List<OrderTrendPoint> orderTrend(String period);

    /**
     * 获取订单状态分布数据。
     *
     * @return 订单状态分布列表
     */
    List<StatusDistribution> orderStatusDistribution();

    /**
     * 获取支付方式分布数据。
     *
     * @return 支付方式分布列表
     */
    List<PaymentDistribution> paymentDistribution();

    /**
     * 获取热门商品排行。
     *
     * @param limit 返回数量上限
     * @return 热门商品列表
     */
    List<HotProductRank> hotProducts(int limit);

    /**
     * 获取商品退货率排行。
     *
     * @param limit 返回数量上限
     * @return 退货率统计列表
     */
    List<ReturnRateStat> productReturnRates(int limit);
}
