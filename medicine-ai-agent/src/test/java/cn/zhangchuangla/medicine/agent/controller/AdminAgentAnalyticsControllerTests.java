package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentAnalyticsController;
import cn.zhangchuangla.medicine.agent.model.vo.analytics.*;
import cn.zhangchuangla.medicine.agent.spi.AdminAnalyticsDataProvider;
import cn.zhangchuangla.medicine.agent.spi.AgentSpiLoader;
import cn.zhangchuangla.medicine.agent.spi.test.TestAgentSpiData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdminAgentAnalyticsController 单元测试。
 */
class AdminAgentAnalyticsControllerTests {

    private final AdminAgentAnalyticsController controller = new AdminAgentAnalyticsController();

    @BeforeEach
    void setUp() {
        TestAgentSpiData.reset();
        prepareTestData();
    }

    @AfterEach
    void tearDown() {
        TestAgentSpiData.reset();
    }

    private void prepareTestData() {
        // 准备订单趋势测试数据
        OrderTrendPoint trendPoint1 = new OrderTrendPoint();
        trendPoint1.setLabel("2025-02-15");
        trendPoint1.setOrderCount(10L);
        trendPoint1.setOrderAmount(new BigDecimal("1000.00"));

        OrderTrendPoint trendPoint2 = new OrderTrendPoint();
        trendPoint2.setLabel("2025-02-14");
        trendPoint2.setOrderCount(8L);
        trendPoint2.setOrderAmount(new BigDecimal("800.00"));

        TestAgentSpiData.orderTrendPoints = List.of(trendPoint1, trendPoint2);

        // 准备订单状态分布测试数据
        StatusDistribution statusDist1 = new StatusDistribution();
        statusDist1.setStatus("PENDING_PAYMENT");
        statusDist1.setStatusName("待支付");
        statusDist1.setCount(5L);

        StatusDistribution statusDist2 = new StatusDistribution();
        statusDist2.setStatus("PAID");
        statusDist2.setStatusName("已支付");
        statusDist2.setCount(40L);

        TestAgentSpiData.statusDistributions = List.of(statusDist1, statusDist2);

        // 准备支付方式分布测试数据
        PaymentDistribution paymentDist1 = new PaymentDistribution();
        paymentDist1.setPayType("ALIPAY");
        paymentDist1.setPayTypeName("支付宝");
        paymentDist1.setCount(30L);
        paymentDist1.setAmount(new BigDecimal("3000.00"));

        PaymentDistribution paymentDist2 = new PaymentDistribution();
        paymentDist2.setPayType("WECHAT");
        paymentDist2.setPayTypeName("微信");
        paymentDist2.setCount(10L);
        paymentDist2.setAmount(new BigDecimal("1000.00"));

        TestAgentSpiData.paymentDistributions = List.of(paymentDist1, paymentDist2);

        // 准备热销商品排行测试数据
        HotProductRank hotProduct1 = new HotProductRank();
        hotProduct1.setProductId(1L);
        hotProduct1.setProductName("测试商品1");
        hotProduct1.setQuantity(50L);
        hotProduct1.setAmount(new BigDecimal("500.00"));

        HotProductRank hotProduct2 = new HotProductRank();
        hotProduct2.setProductId(2L);
        hotProduct2.setProductName("测试商品2");
        hotProduct2.setQuantity(30L);
        hotProduct2.setAmount(new BigDecimal("300.00"));

        TestAgentSpiData.hotProductRanks = List.of(hotProduct1, hotProduct2);

        // 准备商品退货率测试数据
        ReturnRateStat returnRateStat = new ReturnRateStat();
        returnRateStat.setProductId(1L);
        returnRateStat.setProductName("测试商品1");
        returnRateStat.setSoldQuantity(50L);
        returnRateStat.setReturnQuantity(2L);
        returnRateStat.setReturnRate(new BigDecimal("0.04"));

        TestAgentSpiData.returnRateStats = List.of(returnRateStat);
    }

    @Test
    void overview_ShouldReturnOverviewData() {
        var result = controller.overview();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());

        OverviewVo overview = result.getData();
        assertEquals(100L, overview.getTotalUsers());
        assertEquals(50L, overview.getTotalOrders());
        assertEquals(40L, overview.getPaidOrders());
        assertEquals(5L, overview.getRefundCount());
        assertEquals(new BigDecimal("5000.00"), overview.getTotalAmount());
        assertEquals(new BigDecimal("100.00"), overview.getAverageAmount());
        assertEquals(new BigDecimal("500.00"), overview.getRefundAmount());

        assertTrue(TestAgentSpiData.capturedAnalyticsOverview);
    }

    @Test
    void orderTrend_WithDayPeriod_ShouldReturnTrendData() {
        var result = controller.orderTrend("DAY");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());

        List<OrderTrendPoint> trendPoints = result.getData();
        assertEquals("2025-02-15", trendPoints.getFirst().getLabel());
        assertEquals(10L, trendPoints.getFirst().getOrderCount());
        assertEquals(new BigDecimal("1000.00"), trendPoints.getFirst().getOrderAmount());

        assertEquals("DAY", TestAgentSpiData.capturedAnalyticsPeriod);
    }

    @Test
    void orderTrend_WithWeekPeriod_ShouldPassPeriodToProvider() {
        controller.orderTrend("WEEK");

        assertEquals("WEEK", TestAgentSpiData.capturedAnalyticsPeriod);
    }

    @Test
    void orderTrend_WithMonthPeriod_ShouldPassPeriodToProvider() {
        controller.orderTrend("MONTH");

        assertEquals("MONTH", TestAgentSpiData.capturedAnalyticsPeriod);
    }

    @Test
    void orderStatusDistribution_ShouldReturnDistributionData() {
        var result = controller.orderStatusDistribution();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());

        List<StatusDistribution> distributions = result.getData();
        assertEquals("PENDING_PAYMENT", distributions.getFirst().getStatus());
        assertEquals("待支付", distributions.getFirst().getStatusName());
        assertEquals(5L, distributions.getFirst().getCount());

        assertTrue(TestAgentSpiData.capturedAnalyticsStatusDistribution);
    }

    @Test
    void paymentDistribution_ShouldReturnPaymentData() {
        var result = controller.paymentDistribution();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());

        List<PaymentDistribution> distributions = result.getData();
        assertEquals("ALIPAY", distributions.getFirst().getPayType());
        assertEquals("支付宝", distributions.getFirst().getPayTypeName());
        assertEquals(30L, distributions.getFirst().getCount());
        assertEquals(new BigDecimal("3000.00"), distributions.getFirst().getAmount());

        assertTrue(TestAgentSpiData.capturedAnalyticsPaymentDistribution);
    }

    @Test
    void hotProducts_WithLimit10_ShouldReturnProductRanks() {
        var result = controller.hotProducts(10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());

        List<HotProductRank> products = result.getData();
        assertEquals(1L, products.getFirst().getProductId());
        assertEquals("测试商品1", products.getFirst().getProductName());
        assertEquals(50L, products.getFirst().getQuantity());
        assertEquals(new BigDecimal("500.00"), products.getFirst().getAmount());

        assertEquals(10, TestAgentSpiData.capturedAnalyticsHotProductsLimit);
    }

    @Test
    void hotProducts_WithDifferentLimit_ShouldPassLimitToProvider() {
        controller.hotProducts(5);

        assertEquals(5, TestAgentSpiData.capturedAnalyticsHotProductsLimit);
    }

    @Test
    void returnRates_WithLimit10_ShouldReturnStats() {
        var result = controller.returnRates(10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());

        List<ReturnRateStat> stats = result.getData();
        assertEquals(1L, stats.getFirst().getProductId());
        assertEquals("测试商品1", stats.getFirst().getProductName());
        assertEquals(50L, stats.getFirst().getSoldQuantity());
        assertEquals(2L, stats.getFirst().getReturnQuantity());
        assertEquals(new BigDecimal("0.04"), stats.getFirst().getReturnRate());

        assertEquals(10, TestAgentSpiData.capturedAnalyticsReturnRatesLimit);
    }

    @Test
    void returnRates_WithDifferentLimit_ShouldPassLimitToProvider() {
        controller.returnRates(5);

        assertEquals(5, TestAgentSpiData.capturedAnalyticsReturnRatesLimit);
    }

    @Test
    void agentSpiLoader_ShouldLoadAnalyticsProvider() {
        assertTrue(AgentSpiLoader.hasImplementation(AdminAnalyticsDataProvider.class));
        assertNotNull(AgentSpiLoader.loadSingle(AdminAnalyticsDataProvider.class));
    }

    @Test
    void allEndpoints_ShouldReturnSuccessCode() {
        assertEquals(200, controller.overview().getCode());
        assertEquals(200, controller.orderTrend("DAY").getCode());
        assertEquals(200, controller.orderStatusDistribution().getCode());
        assertEquals(200, controller.paymentDistribution().getCode());
        assertEquals(200, controller.hotProducts(10).getCode());
        assertEquals(200, controller.returnRates(10).getCode());
    }

    @Test
    void orderTrend_WhenEmptyData_ShouldReturnEmptyList() {
        TestAgentSpiData.orderTrendPoints = List.of();

        var result = controller.orderTrend("DAY");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void hotProducts_WithZeroLimit_ShouldPassZeroToProvider() {
        var result = controller.hotProducts(0);

        assertEquals(200, result.getCode());
        assertEquals(0, TestAgentSpiData.capturedAnalyticsHotProductsLimit);
    }
}
