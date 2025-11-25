package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.tool.*;
import cn.zhangchuangla.medicine.llm.spi.AdminDataProvider;
import cn.zhangchuangla.medicine.llm.spi.AdminDataProviderLoader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 面向大模型的后台数据工具，通过 SPI 从 admin 模块拉取真实数据。
 */
@Component
public class AdminAssistantTools {

    private final AdminDataProviderLoader providerLoader;

    public AdminAssistantTools(AdminDataProviderLoader providerLoader) {
        this.providerLoader = providerLoader;
    }

    /**
     * 统计当前平台注册用户总数
     *
     * @return 用户总数
     */
    @Tool(name = "count_total_users", description = "统计当前平台注册用户总数")
    public long totalUsers() {
        return requireProvider().totalUserCount();
    }

    /**
     * 按订单号查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情快照，包含金额、状态、时间节点和商品列表
     * @throws IllegalArgumentException 当订单号对应的订单不存在时抛出
     */
    @Tool(name = "get_order_by_no", description = "按订单号查询订单详情，包含金额、状态、时间节点和商品列表")
    public AdminOrderSnapshot orderByOrderNo(
            @ToolParam(description = "业务订单号，如 O202411250001") String orderNo) {
        return requireProvider().findOrderByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的订单信息"));
    }

    /**
     * 查看最近的订单列表（按创建时间倒序）
     *
     * @param limit 返回的最大订单条数，默认 10，最大 50
     * @return 最近订单快照列表
     */
    @Tool(name = "list_latest_orders", description = "查看最近的订单列表（按创建时间倒序）")
    public List<AdminOrderSnapshot> latestOrders(
            @ToolParam(description = "返回的最大订单条数，默认 10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().latestOrders(safeLimit);
    }

    /**
     * 获取订单整体概况
     *
     * @return 订单概况快照，包含待支付、待发货、售后中、退款金额等信息
     */
    @Tool(name = "get_order_overview", description = "获取订单整体概况：待支付、待发货、售后中、退款金额等")
    public OrderOverviewSnapshot orderOverview() {
        return requireProvider().orderOverview();
    }

    /**
     * 获取退款/售后概况与最近记录
     *
     * @param recentLimit 需要返回的最新记录条数，默认 5，最大 50
     * @return 退款概况快照，包含退款统计和最近退款记录
     */
    @Tool(name = "get_refund_overview", description = "获取退款/售后概况与最近记录")
    public RefundOverviewSnapshot refundOverview(
            @ToolParam(description = "需要返回的最新记录条数，默认 5") Integer recentLimit) {
        int safeLimit = recentLimit == null || recentLimit <= 0 ? 5 : Math.min(recentLimit, 50);
        return requireProvider().refundOverview(safeLimit);
    }

    /**
     * 按名称关键词搜索商品
     *
     * @param keyword 商品名称关键词
     * @param limit   返回的最大条数，默认 10，最大 50
     * @return 商品快照列表，包含价格、库存、商品图片等信息
     */
    @Tool(name = "search_products", description = "按名称关键词搜索商品，返回价格、库存、商品图片等信息")
    public List<ProductSnapshot> searchProducts(
            @ToolParam(description = "商品名称关键词") String keyword,
            @ToolParam(description = "返回的最大条数，默认 10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().searchProducts(keyword, safeLimit);
    }

    /**
     * 根据商品ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品快照，包含详细信息
     * @throws IllegalArgumentException 当商品ID对应的商品不存在时抛出
     */
    @Tool(name = "get_product_by_id", description = "根据商品ID查询商品详情")
    public ProductSnapshot getProductById(@ToolParam(description = "商品ID") Long productId) {
        return requireProvider().findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的商品"));
    }

    /**
     * 获取运营分析总览数据
     *
     * @return 分析总览快照，包含用户、订单、退款金额等统计数据
     */
    @Tool(name = "get_analytics_overview", description = "分析-获取运营分析总览数据：用户、订单、退款金额等")
    public AnalyticsOverviewSnapshot analyticsOverview() {
        return requireProvider().analyticsOverview();
    }

    /**
     * 按日/周/月获取订单趋势数据
     *
     * @param period 周期：DAY/WEEK/MONTH，默认 DAY
     * @return 订单趋势点快照列表，便于绘制折线图
     */
    @Tool(name = "get_order_trend", description = "分析-按日/周/月获取订单趋势数据，便于绘制折线图")
    public List<OrderTrendPointSnapshot> orderTrend(
            @ToolParam(description = "周期：DAY/WEEK/MONTH，默认 DAY") String period) {
        String safePeriod = period == null || period.isBlank() ? "DAY" : period.trim().toUpperCase();
        return requireProvider().orderTrend(safePeriod);

    }

    /**
     * 获取订单状态分布
     *
     * @return 状态分布快照列表，包含各状态的订单数量统计
     */
    @Tool(name = "get_order_status_distribution", description = "分析-获取订单状态分布")
    public List<StatusDistributionSnapshot> orderStatusDistribution() {
        return requireProvider().orderStatusDistribution();
    }

    /**
     * 获取支付方式分布
     *
     * @return 支付方式分布快照列表，包含各支付方式的使用统计
     */
    @Tool(name = "get_payment_distribution", description = "分析-获取支付方式分布")
    public List<PaymentDistributionSnapshot> paymentDistribution() {
        return requireProvider().paymentDistribution();
    }

    /**
     * 获取热销商品排行榜
     *
     * @param limit 返回的商品条数，默认 10，最大 50
     * @return 热销商品排行快照列表，按销量和销售额排序
     */
    @Tool(name = "get_hot_products", description = "分析-获取热销商品排行榜，按销量和销售额排序")
    public List<HotProductRankSnapshot> hotProducts(
            @ToolParam(description = "返回的商品条数，默认 10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().hotProducts(safeLimit);
    }

    /**
     * 获取商品退货率排行
     *
     * @param limit 返回的商品条数，默认 10，最大 50
     * @return 退货率统计快照列表，包含售出数和退货率
     */
    @Tool(name = "get_product_return_rates", description = "分析-获取商品退货率排行，含售出数和退货率")
    public List<ReturnRateStatSnapshot> productReturnRates(
            @ToolParam(description = "返回的商品条数，默认 10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().productReturnRates(safeLimit);
    }

    /**
     * 获取聊天中支持的图表类型及适用场景
     *
     * @return 图表描述符列表，包含名称、消息语言标识(type)和推荐场景
     */
    @Tool(name = "list_supported_chart_types", description = "获取聊天中支持的图表类型及适用场景，返回名称、消息语言标识(type)和推荐场景")
    public List<ChartTemplateRegistry.ChartDescriptor> listSupportedCharts() {
        return ChartTemplateRegistry.descriptors();
    }

    /**
     * 根据图表名称或类型获取演示数据与字段注释
     *
     * @param nameOrType 图表名称或语言标识，例如 折线图/line/柱状图/column
     * @return 图表示例数据，便于前端按需渲染
     * @throws IllegalArgumentException 当图表名称/标识为空或找不到对应图表类型时抛出
     */
    @Tool(name = "get_chart_sample_by_name", description = "根据图表名称或类型获取演示数据与字段注释，便于前端按需渲染，不会一次性返回全部图表配置")
    public ChartTemplateRegistry.ChartSample getChartSample(
            @ToolParam(description = "图表名称或语言标识，例如 折线图/line/柱状图/column") String nameOrType) {
        if (nameOrType == null || nameOrType.isBlank()) {
            throw new IllegalArgumentException("图表名称/标识不能为空");
        }
        return ChartTemplateRegistry.sampleByNameOrType(nameOrType)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的图表类型"));
    }

    /**
     * 获取当前系统时间（UTC+8）
     *
     * @return 包含ISO格式、日期和时间的Map，用于回答时间相关问题
     */
    @Tool(name = "current_datetime", description = "获取当前系统时间（UTC+8），用于回答时间相关问题")
    public Map<String, String> currentDateTime() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        return Map.of(
                "iso", now.toString(),
                "date", now.toLocalDate().toString(),
                "time", now.toLocalTime().withNano(0).toString()
        );
    }

    /**
     * 获取Admin数据提供者
     *
     * @return Admin数据提供者实例
     * @throws IllegalStateException 当未发现admin SPI提供者时抛出
     */
    private AdminDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 admin SPI 提供者，请检查 admin 模块是否正确注册"));
    }

}
