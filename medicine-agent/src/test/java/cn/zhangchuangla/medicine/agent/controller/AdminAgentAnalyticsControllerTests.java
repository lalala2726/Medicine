package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.controller.admin.AdminAgentAnalyticsController;
import cn.zhangchuangla.medicine.agent.service.AnalyticsService;
import cn.zhangchuangla.medicine.model.vo.analytics.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAgentAnalyticsControllerTests {

    private final StubAnalyticsService analyticsService = new StubAnalyticsService();
    private final AdminAgentAnalyticsController controller = new AdminAgentAnalyticsController(analyticsService);

    @Test
    void overview_ShouldDelegateToService() {
        OverviewVo overviewVo = new OverviewVo();
        overviewVo.setTotalUsers(100L);
        analyticsService.overview = overviewVo;

        var result = controller.overview();

        assertEquals(200, result.getCode());
        assertEquals(100L, result.getData().getTotalUsers());
        assertTrue(analyticsService.overviewInvoked);
    }

    @Test
    void orderTrend_ShouldDelegateWithPeriod() {
        OrderTrendPoint point = new OrderTrendPoint();
        point.setLabel("2026-02-16");
        point.setOrderCount(8L);
        analyticsService.orderTrend = List.of(point);

        var result = controller.orderTrend("DAY");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("2026-02-16", result.getData().getFirst().getLabel());
        assertEquals("DAY", analyticsService.capturedPeriod);
    }

    @Test
    void orderStatusDistribution_ShouldDelegate() {
        StatusDistribution status = new StatusDistribution();
        status.setStatus("PENDING_PAYMENT");
        analyticsService.statusDistributions = List.of(status);

        var result = controller.orderStatusDistribution();

        assertEquals(200, result.getCode());
        assertEquals("PENDING_PAYMENT", result.getData().getFirst().getStatus());
        assertTrue(analyticsService.statusInvoked);
    }

    @Test
    void paymentDistribution_ShouldDelegate() {
        PaymentDistribution payment = new PaymentDistribution();
        payment.setPayType("ALIPAY");
        payment.setAmount(new BigDecimal("88.00"));
        analyticsService.paymentDistributions = List.of(payment);

        var result = controller.paymentDistribution();

        assertEquals(200, result.getCode());
        assertEquals("ALIPAY", result.getData().getFirst().getPayType());
        assertTrue(analyticsService.paymentInvoked);
    }

    @Test
    void hotProducts_ShouldDelegateLimit() {
        HotProductRank rank = new HotProductRank();
        rank.setProductId(1L);
        analyticsService.hotProducts = List.of(rank);

        var result = controller.hotProducts(5);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getFirst().getProductId());
        assertEquals(5, analyticsService.capturedHotLimit);
    }

    @Test
    void returnRates_ShouldDelegateLimit() {
        ReturnRateStat stat = new ReturnRateStat();
        stat.setProductId(1L);
        analyticsService.returnRates = List.of(stat);

        var result = controller.returnRates(10);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().getFirst().getProductId());
        assertEquals(10, analyticsService.capturedReturnLimit);
    }

    private static class StubAnalyticsService implements AnalyticsService {

        private OverviewVo overview;
        private List<OrderTrendPoint> orderTrend = List.of();
        private List<StatusDistribution> statusDistributions = List.of();
        private List<PaymentDistribution> paymentDistributions = List.of();
        private List<HotProductRank> hotProducts = List.of();
        private List<ReturnRateStat> returnRates = List.of();

        private boolean overviewInvoked;
        private boolean statusInvoked;
        private boolean paymentInvoked;
        private String capturedPeriod;
        private int capturedHotLimit;
        private int capturedReturnLimit;

        @Override
        public OverviewVo overview() {
            this.overviewInvoked = true;
            return overview;
        }

        @Override
        public List<OrderTrendPoint> orderTrend(String period) {
            this.capturedPeriod = period;
            return orderTrend;
        }

        @Override
        public List<StatusDistribution> orderStatusDistribution() {
            this.statusInvoked = true;
            return statusDistributions;
        }

        @Override
        public List<PaymentDistribution> paymentDistribution() {
            this.paymentInvoked = true;
            return paymentDistributions;
        }

        @Override
        public List<HotProductRank> hotProducts(int limit) {
            this.capturedHotLimit = limit;
            return hotProducts;
        }

        @Override
        public List<ReturnRateStat> productReturnRates(int limit) {
            this.capturedReturnLimit = limit;
            return returnRates;
        }
    }
}
