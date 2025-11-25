package cn.zhangchuangla.medicine.admin.spi;

import cn.zhangchuangla.medicine.admin.model.vo.UserDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.analytics.*;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.core.utils.SpringUtils;
import cn.zhangchuangla.medicine.common.security.entity.SysUserDetails;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.llm.model.tool.*;
import cn.zhangchuangla.medicine.llm.spi.AdminDataProvider;
import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import cn.zhangchuangla.medicine.model.entity.*;
import cn.zhangchuangla.medicine.model.enums.AfterSaleStatusEnum;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import lombok.extern.slf4j.Slf4j;

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
public class AdminModuleDataProvider implements AdminDataProvider {

    private static final int PAID_FLAG = 1;

    @Override
    public Optional<AdminUserSnapshot> currentUser() {
        try {
            SysUserDetails loginUser = SecurityUtils.getLoginUser();
            Long userId = loginUser.getUserId();
            UserDetailVo detail = userId == null ? null : userService().getUserDetailById(userId);

            AdminUserSnapshot snapshot = new AdminUserSnapshot();
            snapshot.setUserId(userId);
            snapshot.setUsername(loginUser.getUsername());
            snapshot.setRoles(SecurityUtils.getRoles());

            if (detail != null) {
                snapshot.setNickname(detail.getNickName());
                if (detail.getBasicInfo() != null) {
                    snapshot.setPhoneNumber(detail.getBasicInfo().getPhoneNumber());
                    snapshot.setEmail(detail.getBasicInfo().getEmail());
                }
                if (detail.getSecurityInfo() != null) {
                    snapshot.setLastLoginIp(detail.getSecurityInfo().getLastLoginIp());
                    snapshot.setLastLoginTime(detail.getSecurityInfo().getLastLoginTime());
                }
                snapshot.setTotalOrders(detail.getTotalOrders() == null ? 0L : detail.getTotalOrders().longValue());
                snapshot.setTotalConsume(defaultBigDecimal(detail.getTotalConsume()));
                snapshot.setWalletBalance(defaultBigDecimal(detail.getWalletBalance()));
            }
            return Optional.of(snapshot);
        } catch (Exception ex) {
            log.warn("Failed to load current admin user info via SPI", ex);
            return Optional.empty();
        }
    }

    @Override
    public long totalUserCount() {
        try {
            return userService().count();
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
            MallOrder order = mallOrderService().getOrderByOrderNo(orderNo.trim());
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
            List<MallOrder> orders = mallOrderService().lambdaQuery()
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
            MallOrderService orderService = mallOrderService();
            OrderOverviewSnapshot snapshot = new OrderOverviewSnapshot();
            snapshot.setTotalOrders(orderService.count());
            snapshot.setPendingPayment(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.PENDING_PAYMENT.getType()).count());
            snapshot.setPendingShipment(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.PENDING_SHIPMENT.getType()).count());
            snapshot.setPendingReceipt(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.PENDING_RECEIPT.getType()).count());
            snapshot.setCompleted(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.COMPLETED.getType()).count());
            snapshot.setRefunded(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.REFUNDED.getType()).count());
            snapshot.setAfterSale(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.AFTER_SALE.getType()).count());
            snapshot.setCancelled(orderService.lambdaQuery().eq(MallOrder::getOrderStatus, OrderStatusEnum.CANCELLED.getType()).count());

            List<MallOrder> paidOrders = orderService.lambdaQuery()
                    .eq(MallOrder::getPaid, PAID_FLAG)
                    .notIn(MallOrder::getOrderStatus, OrderStatusEnum.CANCELLED.getType(), OrderStatusEnum.REFUNDED.getType())
                    .select(MallOrder::getPayAmount)
                    .list();
            snapshot.setTotalSales(sumAmounts(paidOrders.stream()
                    .map(MallOrder::getPayAmount)
                    .toList()));

            List<BigDecimal> refundAmounts = orderService.lambdaQuery()
                    .select(MallOrder::getRefundPrice)
                    .list()
                    .stream()
                    .map(MallOrder::getRefundPrice)
                    .toList();
            snapshot.setRefundedAmount(sumAmounts(refundAmounts));
            return snapshot;
        } catch (Exception ex) {
            log.warn("Failed to build order overview for LLM tool", ex);
            return new OrderOverviewSnapshot();
        }
    }

    @Override
    public RefundOverviewSnapshot refundOverview(int recentLimit) {
        try {
            MallAfterSaleService afterSaleService = mallAfterSaleService();
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

            List<BigDecimal> refundedAmounts = mallOrderService().lambdaQuery()
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
            List<MallProduct> products = mallProductService().lambdaQuery()
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
            MallProduct product = mallProductService().getById(productId);
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
            OverviewVo vo = analyticsService().overview();
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
            List<OrderTrendPoint> points = analyticsService().orderTrend(period);
            if (points == null) {
                return Collections.emptyList();
            }
            return points.stream().map(p -> {
                OrderTrendPointSnapshot snap = new OrderTrendPointSnapshot();
                snap.setLabel(p.getLabel());
                snap.setOrderCount(defaultLong(p.getOrderCount()));
                snap.setOrderAmount(defaultBigDecimal(p.getOrderAmount()));
                return snap;
            }).toList();
        } catch (Exception ex) {
            log.warn("Failed to load order trend for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<StatusDistributionSnapshot> orderStatusDistribution() {
        try {
            List<StatusDistribution> list = analyticsService().orderStatusDistribution();
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> {
                StatusDistributionSnapshot snap = new StatusDistributionSnapshot();
                snap.setStatus(item.getStatus());
                snap.setStatusName(item.getStatusName());
                snap.setCount(defaultLong(item.getCount()));
                return snap;
            }).toList();
        } catch (Exception ex) {
            log.warn("Failed to load order status distribution for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PaymentDistributionSnapshot> paymentDistribution() {
        try {
            List<PaymentDistribution> list = analyticsService().paymentDistribution();
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> {
                PaymentDistributionSnapshot snap = new PaymentDistributionSnapshot();
                snap.setPayType(item.getPayType());
                snap.setPayTypeName(item.getPayTypeName());
                snap.setCount(defaultLong(item.getCount()));
                snap.setAmount(defaultBigDecimal(item.getAmount()));
                return snap;
            }).toList();
        } catch (Exception ex) {
            log.warn("Failed to load payment distribution for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HotProductRankSnapshot> hotProducts(int limit) {
        try {
            int safeLimit = normalizeLimit(limit);
            List<HotProductRank> list = analyticsService().hotProducts(safeLimit);
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> {
                HotProductRankSnapshot snap = new HotProductRankSnapshot();
                snap.setProductId(item.getProductId());
                snap.setProductName(item.getProductName());
                snap.setQuantity(defaultLong(item.getQuantity()));
                snap.setAmount(defaultBigDecimal(item.getAmount()));
                return snap;
            }).toList();
        } catch (Exception ex) {
            log.warn("Failed to load hot products for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ReturnRateStatSnapshot> productReturnRates(int limit) {
        try {
            int safeLimit = normalizeLimit(limit);
            List<ReturnRateStat> list = analyticsService().productReturnRates(safeLimit);
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream().map(item -> {
                ReturnRateStatSnapshot snap = new ReturnRateStatSnapshot();
                snap.setProductId(item.getProductId());
                snap.setProductName(item.getProductName());
                snap.setSoldQuantity(defaultLong(item.getSoldQuantity()));
                snap.setReturnQuantity(defaultLong(item.getReturnQuantity()));
                snap.setReturnRate(item.getReturnRate() == null ? BigDecimal.ZERO : item.getReturnRate());
                return snap;
            }).toList();
        } catch (Exception ex) {
            log.warn("Failed to load product return rates for LLM tool", ex);
            return Collections.emptyList();
        }
    }

    private AdminOrderSnapshot buildOrderSnapshot(MallOrder order) {
        AdminOrderSnapshot snapshot = new AdminOrderSnapshot();
        snapshot.setOrderNo(order.getOrderNo());
        snapshot.setOrderStatus(order.getOrderStatus());
        snapshot.setPayType(order.getPayType());
        snapshot.setPaid(order.getPaid());
        snapshot.setTotalAmount(order.getTotalAmount());
        snapshot.setPayAmount(order.getPayAmount());
        snapshot.setRefundAmount(order.getRefundPrice());
        snapshot.setRefundStatus(order.getRefundStatus());
        snapshot.setReceiverName(order.getReceiverName());
        snapshot.setReceiverPhone(order.getReceiverPhone());
        snapshot.setReceiverDetail(order.getReceiverDetail());
        snapshot.setCreateTime(order.getCreateTime());
        snapshot.setPayTime(order.getPayTime());
        snapshot.setDeliverTime(order.getDeliverTime());
        snapshot.setReceiveTime(order.getReceiveTime());
        snapshot.setRefundTime(order.getRefundTime());

        snapshot.setItems(loadOrderItems(order.getId()));
        return snapshot;
    }

    private List<AdminOrderItemSnapshot> loadOrderItems(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        List<MallOrderItem> items = mallOrderItemService().getOrderItemByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(item -> {
            AdminOrderItemSnapshot snapshot = new AdminOrderItemSnapshot();
            snapshot.setProductName(item.getProductName());
            snapshot.setQuantity(item.getQuantity());
            snapshot.setPrice(item.getPrice());
            snapshot.setTotalPrice(item.getTotalPrice());
            snapshot.setAfterSaleStatus(item.getAfterSaleStatus());
            snapshot.setImages(loadFirstImages(item.getProductId()));
            return snapshot;
        }).toList();
    }

    private RefundRecordSnapshot buildRefundRecord(MallAfterSale record) {
        RefundRecordSnapshot snapshot = new RefundRecordSnapshot();
        snapshot.setAfterSaleNo(record.getAfterSaleNo());
        snapshot.setOrderNo(record.getOrderNo());
        snapshot.setAfterSaleType(record.getAfterSaleType() == null ? null : record.getAfterSaleType().getType());
        snapshot.setAfterSaleStatus(record.getAfterSaleStatus() == null ? null : record.getAfterSaleStatus().getStatus());
        snapshot.setRefundAmount(record.getRefundAmount());
        snapshot.setApplyTime(record.getApplyTime());
        snapshot.setCompleteTime(record.getCompleteTime());
        snapshot.setApplyReason(record.getApplyReason() == null ? null : record.getApplyReason().getName());
        return snapshot;
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

        List<ProductSnapshot> snapshots = products.stream().map(p -> {
            ProductSnapshot snapshot = new ProductSnapshot();
            snapshot.setId(p.getId());
            snapshot.setName(p.getName());
            snapshot.setPrice(p.getPrice());
            snapshot.setStock(p.getStock());
            snapshot.setStatus(p.getStatus());
            snapshot.setDeliveryType(p.getDeliveryType());
            return snapshot;
        }).toList();

        if (!ids.isEmpty()) {
            var images = mallProductImageService().getFirstImageByProductIds(ids).stream()
                    .collect(Collectors.groupingBy(MallProductImage::getProductId, Collectors.mapping(MallProductImage::getImageUrl, Collectors.toList())));
            snapshots.forEach(s -> s.setImages(images.getOrDefault(s.getId(), Collections.emptyList())));

            var details = mallMedicineDetailService().lambdaQuery()
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
        return mallProductImageService().getFirstImageByProductIds(List.of(productId)).stream()
                .map(MallProductImage::getImageUrl)
                .toList();
    }

    private UserService userService() {
        return SpringUtils.getBean(UserService.class);
    }

    private MallOrderService mallOrderService() {
        return SpringUtils.getBean(MallOrderService.class);
    }

    private MallOrderItemService mallOrderItemService() {
        return SpringUtils.getBean(MallOrderItemService.class);
    }

    private MallAfterSaleService mallAfterSaleService() {
        return SpringUtils.getBean(MallAfterSaleService.class);
    }

    private MallProductService mallProductService() {
        return SpringUtils.getBean(MallProductService.class);
    }

    private MallProductImageService mallProductImageService() {
        return SpringUtils.getBean(MallProductImageService.class);
    }

    private MallMedicineDetailService mallMedicineDetailService() {
        return SpringUtils.getBean(MallMedicineDetailService.class);
    }

    private AnalyticsService analyticsService() {
        return SpringUtils.getBean(AnalyticsService.class);
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
