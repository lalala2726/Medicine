package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.AnalyticsMapper;
import cn.zhangchuangla.medicine.admin.model.vo.analytics.*;
import cn.zhangchuangla.medicine.admin.service.AnalyticsService;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import cn.zhangchuangla.medicine.model.enums.PayTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsMapper analyticsMapper;

    @Override
    public OverviewVo overview() {
        OverviewVo vo = new OverviewVo();
        vo.setTotalUsers(defaultLong(analyticsMapper.countUsers()));
        vo.setTotalOrders(defaultLong(analyticsMapper.countOrders()));

        OrderAmountStats amountStats = analyticsMapper.orderAmountStats();
        if (amountStats != null) {
            vo.setPaidOrders(defaultLong(amountStats.getPaidOrders()));
            vo.setTotalAmount(defaultBigDecimal(amountStats.getTotalAmount()));
            vo.setAverageAmount(defaultBigDecimal(amountStats.getAverageAmount()));
        }

        RefundStats refundStats = analyticsMapper.refundStats();
        if (refundStats != null) {
            vo.setRefundCount(defaultLong(refundStats.getRefundCount()));
            vo.setRefundAmount(defaultBigDecimal(refundStats.getRefundAmount()));
        }
        return vo;
    }

    @Override
    public List<OrderTrendPoint> orderTrend(String period) {
        PeriodConfig config = resolvePeriod(period);
        List<OrderTrendPoint> points = analyticsMapper.orderTrend(config.startTime(), config.groupFormat());
        if (CollectionUtils.isEmpty(points)) {
            return List.of();
        }
        return points;
    }

    @Override
    public List<StatusDistribution> orderStatusDistribution() {
        List<StatusDistribution> list = analyticsMapper.orderStatusDistribution();
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        Map<String, OrderStatusEnum> statusMap = Stream.of(OrderStatusEnum.values())
                .collect(Collectors.toMap(OrderStatusEnum::getType, e -> e));
        list.forEach(item -> {
            OrderStatusEnum en = statusMap.get(item.getStatus());
            item.setStatusName(en == null ? "未知" : en.getName());
            item.setCount(defaultLong(item.getCount()));
        });
        return list;
    }

    @Override
    public List<PaymentDistribution> paymentDistribution() {
        List<PaymentDistribution> list = analyticsMapper.paymentDistribution();
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        Map<String, PayTypeEnum> payMap = Stream.of(PayTypeEnum.values())
                .collect(Collectors.toMap(PayTypeEnum::getType, e -> e));
        list.forEach(item -> {
            PayTypeEnum en = payMap.get(item.getPayType());
            item.setPayTypeName(en == null ? "未知" : en.getType());
            item.setCount(defaultLong(item.getCount()));
            item.setAmount(defaultBigDecimal(item.getAmount()));
        });
        return list;
    }

    @Override
    public List<HotProductRank> hotProducts(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        List<HotProductRank> list = analyticsMapper.hotProducts(safeLimit);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        list.forEach(item -> {
            item.setQuantity(defaultLong(item.getQuantity()));
            item.setAmount(defaultBigDecimal(item.getAmount()));
            item.setSalesVolume(defaultLong(item.getSalesVolume()));
        });
        return list;
    }

    @Override
    public List<ReturnRateStat> productReturnRates(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        List<ReturnRateStat> list = analyticsMapper.productReturnRates(safeLimit);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        list.forEach(item -> {
            long sold = defaultLong(item.getSoldQuantity());
            long returned = defaultLong(item.getReturnQuantity());
            item.setSoldQuantity(sold);
            item.setReturnQuantity(returned);
            if (sold == 0) {
                item.setReturnRate(BigDecimal.ZERO);
            } else {
                item.setReturnRate(BigDecimal.valueOf(returned)
                        .divide(BigDecimal.valueOf(sold), 4, RoundingMode.HALF_UP));
            }
        });
        return list;
    }

    private PeriodConfig resolvePeriod(String period) {
        String normalized = StringUtils.hasText(period) ? period.trim().toUpperCase() : "DAY";
        LocalDate today = LocalDate.now();
        return switch (normalized) {
            case "WEEK" -> new PeriodConfig(today.minusWeeks(12).atStartOfDay(), "%x-%v");
            case "MONTH" -> new PeriodConfig(today.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(11).atStartOfDay(), "%Y-%m");
            default -> new PeriodConfig(today.minusDays(29).atStartOfDay(), "%Y-%m-%d");
        };
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record PeriodConfig(LocalDateTime startTime, String groupFormat) {
    }
}
