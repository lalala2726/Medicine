package cn.zhangchuangla.medicine.llm.spi;

import cn.zhangchuangla.medicine.llm.model.tool.ProductSnapshot;
import cn.zhangchuangla.medicine.llm.model.tool.admin.*;

import java.util.List;
import java.util.Optional;

/**
 * 通过 SPI 由 admin 模块实现的数据提供接口。
 */
public interface AdminDataProvider {

    /**
     * 获取当前登录管理员的精简信息。
     */
    Optional<UserSnapshotTool> currentUser();

    /**
     * 统计平台当前的用户数量。
     */
    long totalUserCount();

    /**
     * 根据订单号查询订单详情。
     */
    Optional<OrderSnapshotTool> findOrderByOrderNo(String orderNo);

    /**
     * 查询最新订单（按创建时间倒序）。
     */
    List<OrderSnapshotTool> latestOrders(int limit);

    /**
     * 获取订单整体概况。
     */
    OrderOverviewSnapshotTool orderOverview();

    /**
     * 获取售后/退款概况。
     */
    RefundOverviewSnapshotTool refundOverview(int recentLimit);

    /**
     * 根据关键词检索商品。
     */
    List<ProductSnapshot> searchProducts(String keyword, int limit);

    /**
     * 根据 ID 获取商品详情。
     */
    Optional<ProductSnapshot> findProductById(Long productId);

    /**
     * 获取运营分析总览。
     */
    AnalyticsOverviewSnapshotTool analyticsOverview();

    /**
     * 获取订单趋势数据。
     */
    List<OrderTrendPointSnapshotTool> orderTrend(String period);

    /**
     * 获取订单状态分布。
     */
    List<StatusDistributionSnapshotTool> orderStatusDistribution();

    /**
     * 获取支付方式分布。
     */
    List<PaymentDistributionSnapshotTool> paymentDistribution();

    /**
     * 获取热销商品排行榜。
     */
    List<HotProductRankSnapshotTool> hotProducts(int limit);

    /**
     * 获取商品退货率排行。
     */
    List<ReturnRateStatSnapshotTool> productReturnRates(int limit);
}
