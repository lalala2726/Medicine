package cn.zhangchuangla.medicine.llm.tool;

import cn.zhangchuangla.medicine.llm.model.tool.ProductSnapshot;
import cn.zhangchuangla.medicine.llm.model.tool.admin.*;
import cn.zhangchuangla.medicine.llm.spi.AdminDataProvider;
import cn.zhangchuangla.medicine.llm.spi.AdminDataProviderLoader;
import cn.zhangchuangla.medicine.llm.tool.annotation.ToolCallStage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 面向大模型的后台数据工具
 * 优化策略：增加思维链参数，丰富工具描述场景
 */
@Component
@RequiredArgsConstructor
public class AdminAssistantTools {

    private final AdminDataProviderLoader providerLoader;


    /**
     * 统计平台当前注册用户总量。
     *
     * @return 用户数量
     */
    @Tool(name = "count_total_users", description = "统计平台当前的总注册用户数。仅用于回答用户总量的概览问题。")
    @ToolCallStage(start = "正在统计用户总数", end = "用户总数统计完毕")
    public long totalUsers() {
        return requireProvider().totalUserCount();
    }


    /**
     * 依据订单号查询订单快照。
     *
     * @param explanation 调用原因说明
     * @param orderNo     业务订单号
     * @return 订单快照
     */
    @Tool(name = "get_order_by_no", description = """
            <功能>：按订单号精准查询订单详情。
            <参数>：orderNo (必须是完整的业务单号，通常以 'O' 开头)。
            <何时使用>：用户提供了明确的订单号（如 '查一下订单 O2024...'）。
            <何时不使用>：用户只说了'最近的订单'或'我的订单'但没给单号（此时应使用 list_latest_orders）。
            """)
    @ToolCallStage(start = "正在查询订单信息", end = "订单信息查询完毕")
    public OrderSnapshotTool orderByOrderNo(
            @ToolParam(description = "解释为什么调用此工具（例如：用户提供了具体单号 Oxxxx）") String explanation,
            @ToolParam(description = "业务订单号，如 O202411250001") String orderNo) {
        OrderSnapshotTool orderSnapshotTool = requireProvider().findOrderByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的订单信息"));
        return orderSnapshotTool;
    }

    /**
     * 获取最近生成的订单列表。
     *
     * @param limit 返回条数（默认 10，最大 50）
     * @return 订单快照列表
     */
    @Tool(name = "list_latest_orders", description = "查询最近生成的订单列表（倒序）。用于用户想看'最新订单'、'刚才下的单'等场景。")
    @ToolCallStage(start = "正在查询最新订单列表", end = "最新订单列表查询完毕")
    public List<OrderSnapshotTool> latestOrders(
            @ToolParam(description = "返回数量，默认为10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        List<OrderSnapshotTool> orders = requireProvider().latestOrders(safeLimit);
        return orders;
    }

    /**
     * 获取订单看板宏观数据。
     *
     * @return 订单概览数据
     */
    @Tool(name = "get_order_overview", description = "获取订单看板数据：包括待支付数、待发货数、售后中订单数、今日退款金额等宏观数据。")
    @ToolCallStage(start = "正在获取订单看板数据", end = "订单看板数据获取完毕")
    public OrderOverviewSnapshotTool orderOverview() {
        OrderOverviewSnapshotTool overview = requireProvider().orderOverview();
        return overview;
    }

    /**
     * 获取售后与退款概况。
     *
     * @param recentLimit 最新记录条数（默认 5，最大 50）
     * @return 退款概览数据
     */
    @Tool(name = "get_refund_overview", description = "获取售后与退款板块的概况，包含最新的几条退款记录。")
    @ToolCallStage(start = "正在获取售后退款概况", end = "售后退款概况获取完毕")
    public RefundOverviewSnapshotTool refundOverview(
            @ToolParam(description = "最新记录条数，默认5") Integer recentLimit) {
        int safeLimit = recentLimit == null || recentLimit <= 0 ? 5 : Math.min(recentLimit, 50);
        RefundOverviewSnapshotTool overview = requireProvider().refundOverview(safeLimit);
        return overview;
    }


    /**
     * 根据关键字模糊搜索商品。
     *
     * @param explanation 调用原因说明
     * @param keyword     商品名称关键词
     * @param limit       最大条数（默认 10，最大 50）
     * @return 商品快照列表
     */
    @Tool(name = "search_products", description = """
            <功能>：模糊搜索商品。
            <何时使用>：用户想查找商品但不知道ID，只描述了名称（如'感冒灵'、'阿莫西林'）。
            """)
    @ToolCallStage(start = "正在搜索商品", end = "商品搜索完毕")
    public List<ProductSnapshot> searchProducts(
            @ToolParam(description = "思维链：用户想找什么商品？") String explanation,
            @ToolParam(description = "商品名称关键词") String keyword,
            @ToolParam(description = "最大条数，默认10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().searchProducts(keyword, safeLimit);
    }

    /**
     * 通过商品 ID 获取商品详情。
     *
     * @param productId 商品 ID
     * @return 商品快照
     */
    @Tool(name = "get_product_by_id", description = "根据精确的商品ID获取详情。仅在已知ID的情况下使用。")
    @ToolCallStage(start = "正在查询商品详情", end = "商品详情查询完毕")
    public ProductSnapshot getProductById(@ToolParam(description = "商品ID") Long productId) {
        return requireProvider().findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的商品"));
    }


    /**
     * 获取运营大盘核心指标。
     *
     * @return 运营概览数据
     */
    @Tool(name = "get_analytics_overview", description = "获取运营大盘数据：用户总数、今日订单数、今日销售额、退款率等核心指标。")
    @ToolCallStage(start = "正在获取运营大盘数据", end = "运营大盘数据获取完毕")
    public AnalyticsOverviewSnapshotTool analyticsOverview() {
        return requireProvider().analyticsOverview();
    }

    /**
     * 获取订单趋势数据。
     *
     * @param period 周期：DAY/WEEK/MONTH，默认 DAY
     * @return 趋势点列表
     */
    @Tool(name = "get_order_trend", description = """
            <功能>：获取订单趋势数据（折线图数据源）。
            <场景>：用户询问'最近一周的销量趋势'、'上个月的订单走势'。
            <注意>：返回的是趋势点列表，后续通常需要配合图表生成工具使用。
            """)
    @ToolCallStage(start = "正在获取订单趋势数据", end = "订单趋势数据获取完毕")
    public List<OrderTrendPointSnapshotTool> orderTrend(
            @ToolParam(description = "周期：DAY(日)/WEEK(周)/MONTH(月)，默认DAY") String period) {
        String safePeriod = period == null || period.isBlank() ? "DAY" : period.trim().toUpperCase();
        return requireProvider().orderTrend(safePeriod);
    }

    /**
     * 获取订单状态分布数据。
     *
     * @return 状态分布列表
     */
    @Tool(name = "get_order_status_distribution", description = "获取订单状态分布（饼图数据源）：各状态（待发货/已完成等）的订单数量。")
    @ToolCallStage(start = "正在获取订单状态分布", end = "订单状态分布获取完毕")
    public List<StatusDistributionSnapshotTool> orderStatusDistribution() {
        return requireProvider().orderStatusDistribution();
    }

    /**
     * 获取支付方式分布数据。
     *
     * @return 支付分布列表
     */
    @Tool(name = "get_payment_distribution", description = "获取支付方式分布（饼图/柱状图数据源）：微信支付、支付宝等的占比。")
    @ToolCallStage(start = "正在获取支付方式分布", end = "支付方式分布获取完毕")
    public List<PaymentDistributionSnapshotTool> paymentDistribution() {
        return requireProvider().paymentDistribution();
    }

    /**
     * 获取热销商品榜单。
     *
     * @param limit 返回条数（默认 10，最大 50）
     * @return 热销商品列表
     */
    @Tool(name = "get_hot_products", description = "获取热销商品榜单（Top N）。用于分析爆款商品。")
    @ToolCallStage(start = "正在获取热销商品榜单", end = "热销商品榜单获取完毕")
    public List<HotProductRankSnapshotTool> hotProducts(
            @ToolParam(description = "返回条数，默认10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().hotProducts(safeLimit);
    }

    /**
     * 获取高退货率商品排行。
     *
     * @param limit 返回条数（默认 10，最大 50）
     * @return 退货率排行列表
     */
    @Tool(name = "get_product_return_rates", description = "获取高退货率商品排行。用于分析异常商品。")
    @ToolCallStage(start = "正在获取高退货率商品排行", end = "高退货率商品排行获取完毕")
    public List<ReturnRateStatSnapshotTool> productReturnRates(
            @ToolParam(description = "返回条数，默认10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().productReturnRates(safeLimit);
    }


    /**
     * 列出支持的图表类型。
     *
     * @return 图表描述列表
     */
    @Tool(name = "list_supported_chart_types", description = """
            <步骤1>：获取系统支持的图表类型。
            当用户要求'画图'、'展示图表'或数据适合可视化时，**首先**调用此工具查看支持哪些图表（如折线图、柱状图）。
            """)
    @ToolCallStage(start = "正在获取支持的图表类型", end = "支持的图表类型获取完毕")
    public List<ChartTemplateRegistry.ChartDescriptor> listSupportedCharts() {
        return ChartTemplateRegistry.descriptors();
    }

    /**
     * 根据名称或类型获取图表配置模板。
     *
     * @param explanation 调用原因说明
     * @param nameOrType  图表名称或类型
     * @return 图表配置模板
     */
    @Tool(name = "get_chart_sample_by_name", description = """
            <步骤2>：获取图表配置模板。
            在调用 'list_supported_chart_types' 选定图表类型后，调用此工具获取该图表的 JSON 结构模板。
            **严禁**自行编造图表配置，必须使用此接口返回的结构。
            """)
    @ToolCallStage(start = "正在获取图表配置模板", end = "图表配置模板获取完毕")
    public ChartTemplateRegistry.ChartSample getChartSample(
            @ToolParam(description = "思维链：准备绘制什么图？") String explanation,
            @ToolParam(description = "图表名称或标识(type)，如 'line' 或 '折线图'") String nameOrType) {
        if (nameOrType == null || nameOrType.isBlank()) {
            throw new IllegalArgumentException("图表名称/标识不能为空");
        }
        return ChartTemplateRegistry.sampleByNameOrType(nameOrType)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的图表类型"));
    }


    /**
     * 获取已注册的后台数据提供者。
     *
     * @return SPI 提供者
     */
    private AdminDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 admin SPI 提供者，请检查 admin 模块是否正确注册"));
    }
}
