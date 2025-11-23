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

    @Tool(name = "get_current_admin_user", description = "获取当前登录管理员的基础信息（含昵称、手机号、登录时间、累计订单等）")
    public AdminUserSnapshot currentUser() {
        return requireProvider().currentUser()
                .orElseThrow(() -> new IllegalStateException("未获取到当前管理员信息"));
    }

    @Tool(name = "count_total_users", description = "统计当前平台注册用户总数")
    public long totalUsers() {
        return requireProvider().totalUserCount();
    }

    @Tool(name = "get_order_by_no", description = "按订单号查询订单详情，包含金额、状态、时间节点和商品列表")
    public AdminOrderSnapshot orderByOrderNo(
            @ToolParam(description = "业务订单号，如 O202411250001") String orderNo) {
        return requireProvider().findOrderByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的订单信息"));
    }

    @Tool(name = "list_latest_orders", description = "查看最近的订单列表（按创建时间倒序）")
    public List<AdminOrderSnapshot> latestOrders(
            @ToolParam(description = "返回的最大订单条数，默认 10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().latestOrders(safeLimit);
    }

    @Tool(name = "get_order_overview", description = "获取订单整体概况：待支付、待发货、售后中、退款金额等")
    public OrderOverviewSnapshot orderOverview() {
        return requireProvider().orderOverview();
    }

    @Tool(name = "get_refund_overview", description = "获取退款/售后概况与最近记录")
    public RefundOverviewSnapshot refundOverview(
            @ToolParam(description = "需要返回的最新记录条数，默认 5") Integer recentLimit) {
        int safeLimit = recentLimit == null || recentLimit <= 0 ? 5 : Math.min(recentLimit, 50);
        return requireProvider().refundOverview(safeLimit);
    }

    @Tool(name = "search_products", description = "按名称关键词搜索商品，返回价格、库存、销量等")
    public List<ProductSnapshot> searchProducts(
            @ToolParam(description = "商品名称关键词") String keyword,
            @ToolParam(description = "返回的最大条数，默认 10") Integer limit) {
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        return requireProvider().searchProducts(keyword, safeLimit);
    }

    @Tool(name = "get_product_by_id", description = "根据商品ID查询商品详情")
    public ProductSnapshot getProductById(@ToolParam(description = "商品ID") Long productId) {
        return requireProvider().findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的商品"));
    }

    @Tool(name = "current_datetime", description = "获取当前系统时间（UTC+8），用于回答时间相关问题")
    public Map<String, String> currentDateTime() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        return Map.of(
                "iso", now.toString(),
                "date", now.toLocalDate().toString(),
                "time", now.toLocalTime().withNano(0).toString()
        );
    }

    private AdminDataProvider requireProvider() {
        return providerLoader.getProvider()
                .orElseThrow(() -> new IllegalStateException("未发现 admin SPI 提供者，请检查 admin 模块是否正确注册"));
    }
}
