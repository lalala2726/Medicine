package cn.zhangchuangla.medicine.admin.spi;

import cn.zhangchuangla.medicine.admin.model.dto.OrderOverviewStats;
import cn.zhangchuangla.medicine.admin.model.vo.UserDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.analytics.*;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.llm.model.tool.*;
import cn.zhangchuangla.medicine.llm.spi.AdminDataProvider;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.entity.*;
import cn.zhangchuangla.medicine.model.enums.AfterSaleStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * admin 模块对 LLM 的数据暴露实现，通过 SPI 被 medicine-llm 发现。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminModuleDataProvider implements AdminDataProvider {

    private final UserService userService;
    private final MallOrderService mallOrderService;
    private final MallOrderItemService mallOrderItemService;
    private final MallAfterSaleService mallAfterSaleService;
    private final MallProductService mallProductService;
    private final MallProductImageService mallProductImageService;
    private final MallMedicineDetailService mallMedicineDetailService;
    private final AnalyticsService analyticsService;

    @Override
    public Optional<AdminUserSnapshot> currentUser() {
        try {
            SysUserDetails loginUser = SecurityUtils.getLoginUser();
            Long userId = loginUser.getUserId();
            UserDetailVo detail = userId == null ? null : userService.getUserDetailById(userId);
            AdminUserSnapshot.AdminUserSnapshotBuilder builder = AdminUserSnapshot.builder()
                    .userId(userId)
                    .username(loginUser.getUsername())
                    .roles(SecurityUtils.getRoles());

            if (detail != null) {
                builder.nickname(detail.getNickName());
                if (detail.getBasicInfo() != null) {
                    builder.phoneNumber(detail.getBasicInfo().getPhoneNumber());
                    builder.email(detail.getBasicInfo().getEmail());
                }
                if (detail.getSecurityInfo() != null) {
                    builder.lastLoginIp(detail.getSecurityInfo().getLastLoginIp());
                    builder.lastLoginTime(detail.getSecurityInfo().getLastLoginTime());
                }
                builder.totalOrders(detail.getTotalOrders() == null ? 0L : detail.getTotalOrders().longValue());
                builder.totalConsume(defaultBigDecimal(detail.getTotalConsume()));
                builder.walletBalance(defaultBigDecimal(detail.getWalletBalance()));
            }
            return Optional.of(builder.build());
        } catch (Exception ex) {
            log.warn("Failed to load current admin user info via SPI", ex);
            return Optional.empty();
        }
    }

    @Override
    public long totalUserCount() {
        try {
            return userService.count();
        } catch (Exception ex) {
            log.warn("Failed to count users for LLM tool", ex);
            return 0L;
        }
    }

    @Override
    public Optional<AdminOrderSnapshot> findOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return Optional.empty();
        }
        try {
            MallOrder order = mallOrderService.getOrderByOrderNo(orderNo.trim());
            if (order == null) {
                return Optional.empty();
            }
            return Optional.of(buildOrderSnapshot(order));
        } catch (Exception ex) {
            log.warn("Failed to fetch order {} for LLM tool", orderNo, ex);
            return Optional.empty();
        }
    }

    @Override
    public List<AdminOrderSnapshot> latestOrders(int limit) {
        try {
            List<MallOrder> orders = mallOrderService.lambdaQuery()
                    .orderByDesc(MallOrder::getCreateTime)
                    .last("LIMIT " + limit)
                    .list();
            if (orders == null) {
                return Collections.emptyList();
            }
            return orders.stream()
                    .map(this::buildOrderSnapshot)
                    .toList();
        } catch (Exception ex) {
            log.warn("Failed to load latest orders for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public OrderOverviewSnapshot orderOverview() {
        try {
            OrderOverviewStats stats = Optional.ofNullable(mallOrderService.getOrderOverviewStats())
                    .orElseGet(() -> OrderOverviewStats.builder()
                            .totalOrders(0L)
                            .pendingPayment(0L)
                            .pendingShipment(0L)
                            .pendingReceipt(0L)
                            .completed(0L)
                            .refunded(0L)
                            .afterSale(0L)
                            .cancelled(0L)
                            .totalSales(BigDecimal.ZERO)
                            .refundedAmount(BigDecimal.ZERO)
                            .build());

            return OrderOverviewSnapshot.builder()
                    .totalOrders(stats.getTotalOrders())
                    .pendingPayment(stats.getPendingPayment())
                    .pendingShipment(stats.getPendingShipment())
                    .pendingReceipt(stats.getPendingReceipt())
                    .completed(stats.getCompleted())
                    .refunded(stats.getRefunded())
                    .afterSale(stats.getAfterSale())
                    .cancelled(stats.getCancelled())
                    .totalSales(defaultBigDecimal(stats.getTotalSales()))
                    .refundedAmount(defaultBigDecimal(stats.getRefundedAmount()))
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to build order overview for LLM tool", ex);
            return new OrderOverviewSnapshot();
        }
    }

    @Override
    public RefundOverviewSnapshot refundOverview(int recentLimit) {
        try {
            MallAfterSaleService afterSaleService = mallAfterSaleService;
            RefundOverviewSnapshot snapshot = new RefundOverviewSnapshot();
            snapshot.setPending(afterSaleService.lambdaQuery()
                    .eq(MallAfterSale::getAfterSaleStatus, AfterSaleStatusEnum.PENDING)
                    .count());
            snapshot.setProcessing(afterSaleService.lambdaQuery()
                    .in(MallAfterSale::getAfterSaleStatus, List.of(AfterSaleStatusEnum.APPROVED, AfterSaleStatusEnum.PROCESSING))
                    .count());
            snapshot.setCompleted(afterSaleService.lambdaQuery()
                    .eq(MallAfterSale::getAfterSaleStatus, AfterSaleStatusEnum.COMPLETED)
                    .count());
            snapshot.setRejected(afterSaleService.lambdaQuery()
                    .eq(MallAfterSale::getAfterSaleStatus, AfterSaleStatusEnum.REJECTED)
                    .count());

            List<BigDecimal> requestedAmounts = afterSaleService.lambdaQuery()
                    .select(MallAfterSale::getRefundAmount)
                    .list()
                    .stream()
                    .map(MallAfterSale::getRefundAmount)
                    .toList();
            snapshot.setTotalRequestedAmount(sumAmounts(requestedAmounts));

            List<BigDecimal> refundedAmounts = mallOrderService.lambdaQuery()
                    .select(MallOrder::getRefundPrice)
                    .list()
                    .stream()
                    .map(MallOrder::getRefundPrice)
                    .toList();
            snapshot.setRefundedAmount(sumAmounts(refundedAmounts));

            List<MallAfterSale> records = afterSaleService.lambdaQuery()
                    .orderByDesc(MallAfterSale::getApplyTime)
                    .last("LIMIT " + recentLimit)
                    .list();
            snapshot.setRecentRecords(records == null ? Collections.emptyList()
                    : records.stream().map(this::buildRefundRecord).toList());
            return snapshot;
        } catch (Exception ex) {
            log.warn("Failed to build refund overview for LLM tool", ex);
            return new RefundOverviewSnapshot();
        }
    }

    @Override
    public List<ProductSnapshot> searchProducts(String keyword, int limit) {
        try {
            String like = keyword == null ? "" : keyword.trim();
            List<MallProduct> products = mallProductService.lambdaQuery()
                    .like(!like.isEmpty(), MallProduct::getName, like)
                    .orderByDesc(MallProduct::getUpdateTime)
                    .last("LIMIT " + limit)
                    .list();
            if (products == null || products.isEmpty()) {
                return Collections.emptyList();
            }
            return enrichProducts(products);
        } catch (Exception ex) {
            log.warn("Failed to search products for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<ProductSnapshot> findProductById(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        try {
            MallProduct product = mallProductService.getById(productId);
            if (product == null) {
                return Optional.empty();
            }
            return enrichProducts(List.of(product)).stream().findFirst();
        } catch (Exception ex) {
            log.warn("Failed to load product {} for LLM tool", productId, ex);
            return Optional.empty();
        }
    }

    @Override
    public AnalyticsOverviewSnapshot analyticsOverview() {
        try {
            OverviewVo vo = analyticsService.overview();
            AnalyticsOverviewSnapshot snapshot = new AnalyticsOverviewSnapshot();
            snapshot.setTotalUsers(defaultLong(vo.getTotalUsers()));
            snapshot.setTotalOrders(defaultLong(vo.getTotalOrders()));
            snapshot.setPaidOrders(defaultLong(vo.getPaidOrders()));
            snapshot.setRefundCount(defaultLong(vo.getRefundCount()));
            snapshot.setTotalAmount(defaultBigDecimal(vo.getTotalAmount()));
            snapshot.setAverageAmount(defaultBigDecimal(vo.getAverageAmount()));
            snapshot.setRefundAmount(defaultBigDecimal(vo.getRefundAmount()));
            return snapshot;
        } catch (Exception ex) {
            log.warn("Failed to load analytics overview for LLM tool", ex);
            return new AnalyticsOverviewSnapshot();
        }
    }

    @Override
    public List<OrderTrendPointSnapshot> orderTrend(String period) {
        try {
            List<OrderTrendPoint> points = analyticsService.orderTrend(period);
            if (points == null) {
                return Collections.emptyList();
            }
            return points.stream().map(p -> OrderTrendPointSnapshot.builder()
                    .label(p.getLabel())
                    .orderCount(defaultLong(p.getOrderCount()))
                    .orderAmount(defaultBigDecimal(p.getOrderAmount()))
                    .build()).toList();
        } catch (Exception ex) {
            log.warn("Failed to load order trend for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<StatusDistributionSnapshot> orderStatusDistribution() {
        try {
            List<StatusDistribution> list = analyticsService.orderStatusDistribution();
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> StatusDistributionSnapshot.builder()
                    .status(item.getStatus())
                    .statusName(item.getStatusName())
                    .count(defaultLong(item.getCount()))
                    .build()).toList();
        } catch (Exception ex) {
            log.warn("Failed to load order status distribution for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PaymentDistributionSnapshot> paymentDistribution() {
        try {
            List<PaymentDistribution> list = analyticsService.paymentDistribution();
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> PaymentDistributionSnapshot.builder()
                    .payType(item.getPayType())
                    .payTypeName(item.getPayTypeName())
                    .count(defaultLong(item.getCount()))
                    .amount(defaultBigDecimal(item.getAmount()))
                    .build()).toList();
        } catch (Exception ex) {
            log.warn("Failed to load payment distribution for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HotProductRankSnapshot> hotProducts(int limit) {
        try {
            int safeLimit = normalizeLimit(limit);
            List<HotProductRank> list = analyticsService.hotProducts(safeLimit);
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> HotProductRankSnapshot.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(defaultLong(item.getQuantity()))
                    .amount(defaultBigDecimal(item.getAmount()))
                    .build()).toList();
        } catch (Exception ex) {
            log.warn("Failed to load hot products for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ReturnRateStatSnapshot> productReturnRates(int limit) {
        try {
            int safeLimit = normalizeLimit(limit);
            List<ReturnRateStat> list = analyticsService.productReturnRates(safeLimit);
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> ReturnRateStatSnapshot.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .soldQuantity(defaultLong(item.getSoldQuantity()))
                    .returnQuantity(defaultLong(item.getReturnQuantity()))
                    .returnRate(item.getReturnRate() == null ? BigDecimal.ZERO : item.getReturnRate())
                    .build()).toList();
        } catch (Exception ex) {
            log.warn("Failed to load product return rates for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    private AdminOrderSnapshot buildOrderSnapshot(MallOrder order) {
        return AdminOrderSnapshot.builder()
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .payType(order.getPayType())
                .paid(order.getPaid())
                .totalAmount(order.getTotalAmount())
                .payAmount(order.getPayAmount())
                .refundAmount(order.getRefundPrice())
                .refundStatus(order.getRefundStatus())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverDetail(order.getReceiverDetail())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .deliverTime(order.getDeliverTime())
                .receiveTime(order.getReceiveTime())
                .refundTime(order.getRefundTime())
                .items(loadOrderItems(order.getId()))
                .build();
    }

    private List<AdminOrderItemSnapshot> loadOrderItems(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        List<MallOrderItem> items = mallOrderItemService.getOrderItemByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(item -> AdminOrderItemSnapshot.builder()
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .afterSaleStatus(item.getAfterSaleStatus())
                .images(loadFirstImages(item.getProductId()))
                .build()).toList();
    }

    private RefundRecordSnapshot buildRefundRecord(MallAfterSale record) {
        return RefundRecordSnapshot.builder()
                .afterSaleNo(record.getAfterSaleNo())
                .orderNo(record.getOrderNo())
                .afterSaleType(record.getAfterSaleType() == null ? null : record.getAfterSaleType().getType())
                .afterSaleStatus(record.getAfterSaleStatus() == null ? null : record.getAfterSaleStatus().getStatus())
                .refundAmount(record.getRefundAmount())
                .applyTime(record.getApplyTime())
                .completeTime(record.getCompleteTime())
                .applyReason(record.getApplyReason() == null ? null : record.getApplyReason().getName())
                .build();
    }

    private BigDecimal sumAmounts(List<BigDecimal> amounts) {
        if (amounts == null || amounts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return amounts.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultBigDecimal(BigDecimal source) {
        return source == null ? BigDecimal.ZERO : source;
    }

    private long defaultLong(Long source) {
        return source == null ? 0L : source;
    }

    private int normalizeLimit(int limit) {
        return limit <= 0 ? 10 : Math.min(limit, 50);
    }

    private List<ProductSnapshot> enrichProducts(List<MallProduct> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = products.stream()
                .map(MallProduct::getId)
                .filter(Objects::nonNull)
                .toList();

        List<ProductSnapshot> snapshots = products.stream().map(p -> ProductSnapshot.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .stock(p.getStock())
                .status(p.getStatus())
                .deliveryType(p.getDeliveryType())
                .build()).toList();

        if (!ids.isEmpty()) {
            var images = mallProductImageService.getFirstImageByProductIds(ids).stream()
                    .collect(Collectors.groupingBy(MallProductImage::getProductId, Collectors.mapping(MallProductImage::getImageUrl, Collectors.toList())));
            snapshots.forEach(s -> s.setImages(images.getOrDefault(s.getId(), Collections.emptyList())));

            var details = mallMedicineDetailService.lambdaQuery()
                    .in(DrugDetail::getProductId, ids)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(DrugDetail::getProductId, this::toDrugDetailDto, (a, b) -> a));
            snapshots.forEach(s -> s.setDrugDetail(details.get(s.getId())));
        }
        return snapshots;
    }

    private List<String> loadFirstImages(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        return mallProductImageService.getFirstImageByProductIds(List.of(productId)).stream()
                .map(MallProductImage::getImageUrl)
                .toList();
    }
    private DrugDetailDto toDrugDetailDto(DrugDetail detail) {
        if (detail == null) {
            return null;
        }
        return DrugDetailDto.builder()
                .commonName(detail.getCommonName())
                .composition(detail.getComposition())
                .characteristics(detail.getCharacteristics())
                .packaging(detail.getPackaging())
                .validityPeriod(detail.getValidityPeriod())
                .storageConditions(detail.getStorageConditions())
                .productionUnit(detail.getProductionUnit())
                .approvalNumber(detail.getApprovalNumber())
                .executiveStandard(detail.getExecutiveStandard())
                .originType(detail.getOriginType())
                .isOutpatientMedicine(detail.getIsOutpatientMedicine())
                .warmTips(detail.getWarmTips())
                .brand(detail.getBrand())
                .prescription(detail.getPrescription())
                .efficacy(detail.getEfficacy())
                .usageMethod(detail.getUsageMethod())
                .adverseReactions(detail.getAdverseReactions())
                .precautions(detail.getPrecautions())
                .taboo(detail.getTaboo())
                .instruction(detail.getInstruction())
                .build();
    }
}
