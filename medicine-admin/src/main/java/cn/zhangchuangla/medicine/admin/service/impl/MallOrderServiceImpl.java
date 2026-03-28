package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.admin.mapper.UserMapper;
import cn.zhangchuangla.medicine.admin.model.dto.OrderDetailRow;
import cn.zhangchuangla.medicine.admin.model.dto.UserOrderStatistics;
import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.OrderAddressVo;
import cn.zhangchuangla.medicine.admin.model.vo.OrderPriceVo;
import cn.zhangchuangla.medicine.admin.model.vo.OrderRemarkVo;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.OrderDetailDto;
import cn.zhangchuangla.medicine.model.dto.OrderTimelineDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.*;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.model.vo.OrderShippingVo;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder>
        implements MallOrderService {

    /**
     * 订单支付成功后在库中持久化的标记位。
     */
    private static final int PAID_FLAG = 1;
    /**
     * 完整退款的订单状态标记，兼容支付宝与钱包等多种通道。
     */
    private static final String REFUND_STATUS_SUCCESS = "SUCCESS";
    /**
     * 部分退款的订单状态标记，表示仍有金额未退回。
     */
    private static final String REFUND_STATUS_PARTIAL = "PARTIAL";
    /**
     * 未传递退款原因时使用的默认描述，方便排查后台手工操作。
     */
    private static final String DEFAULT_REFUND_REASON = "管理员发起退款";

    private final MallOrderMapper mallOrderMapper;
    private final UserMapper userMapper;
    private final MallOrderItemService mallOrderItemService;
    private final MallProductImageService mallProductImageService;
    private final AlipayPaymentService alipayPaymentService;
    private final MallOrderTimelineService mallOrderTimelineService;
    private final UserWalletService userWalletService;
    private final MallOrderShippingService mallOrderShippingService;
    private final MallInventoryService mallInventoryService;


    @Override
    public MallOrder getOrderByOrderNo(String orderNo) {
        Assert.isTrue(orderNo != null, "订单号不能为空");
        MallOrder mallOrder = lambdaQuery().eq(MallOrder::getOrderNo, orderNo).one();
        if (mallOrder == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }
        return mallOrder;
    }

    @Override
    public List<OrderDetailDto> getOrderByOrderNo(List<String> orderNos) {
        if (CollectionUtils.isEmpty(orderNos)) {
            return List.of();
        }
        List<String> normalizedOrderNos = orderNos.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (normalizedOrderNos.isEmpty()) {
            return List.of();
        }

        List<OrderDetailRow> rows = mallOrderMapper.selectOrderDetailRowsByOrderNos(normalizedOrderNos);
        if (CollectionUtils.isEmpty(rows)) {
            return List.of();
        }

        Map<String, OrderDetailDto> detailMap = new LinkedHashMap<>();
        for (OrderDetailRow row : rows) {
            if (row == null || !StringUtils.hasText(row.getOrderNo())) {
                continue;
            }
            OrderDetailDto detail = detailMap.computeIfAbsent(row.getOrderNo(), orderNo -> buildOrderDetailDto(row));
            if (row.getOrderItemId() == null) {
                continue;
            }
            detail.getProductInfo().add(OrderDetailDto.ProductInfo.builder()
                    .productId(row.getProductId())
                    .productName(row.getProductName())
                    .productImage(row.getProductImage())
                    .productPrice(row.getProductPrice())
                    .productQuantity(row.getProductQuantity())
                    .productTotalAmount(row.getProductTotalAmount())
                    .build());
        }

        List<OrderDetailDto> result = new ArrayList<>();
        for (String orderNo : normalizedOrderNos) {
            OrderDetailDto detail = detailMap.get(orderNo);
            if (detail != null) {
                result.add(detail);
            }
        }
        return result;
    }

    @Override
    public MallOrder getOrderById(Long id) {
        Assert.isPositive(id, "订单ID不能小于0");
        MallOrder mallOrder = getById(id);
        if (mallOrder == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }
        return mallOrder;
    }

    @Override
    public OrderDetailDto orderDetail(Long orderId) {

        // 获取订单信息
        MallOrder mallOrder = getOrderById(orderId);
        // 获取用户信息
        User userInfo = userMapper.selectById(mallOrder.getUserId());
        // 获取商品信息
        List<MallOrderItem> mallOrderItems = mallOrderItemService.getOrderItemByOrderId(mallOrder.getId());

        // 构建用户信息
        OrderDetailDto.UserInfo userInfoVo = OrderDetailDto.UserInfo.builder()
                .userId(userInfo.getId().toString())
                .nickname(userInfo.getNickname())
                .phoneNumber(userInfo.getPhoneNumber())
                .build();

        // 构建配送信息
        OrderDetailDto.DeliveryInfo deliveryInfo = OrderDetailDto.DeliveryInfo.builder()
                .receiverName(mallOrder.getReceiverName())
                .receiverAddress(mallOrder.getReceiverDetail())
                .receiverPhone(mallOrder.getReceiverPhone())
                .deliveryMethod(getDeliveryTypeDesc(mallOrder.getDeliveryType()))
                .build();

        // 构建订单信息
        OrderDetailDto.OrderInfo orderInfo = OrderDetailDto.OrderInfo.builder()
                .orderNo(mallOrder.getOrderNo())
                .orderStatus(mallOrder.getOrderStatus())
                .payType(mallOrder.getPayType())
                .totalAmount(mallOrder.getTotalAmount())
                .payAmount(mallOrder.getPayAmount())
                .freightAmount(mallOrder.getFreightAmount())
                .build();

        // 构建商品信息
        List<OrderDetailDto.ProductInfo> productInfoLists = new ArrayList<>();
        mallOrderItems.forEach(mallOrderItem -> {
            OrderDetailDto.ProductInfo productInfo = OrderDetailDto.ProductInfo.builder()
                    .productId(mallOrderItem.getProductId())
                    .productName(mallOrderItem.getProductName())
                    .productImage(mallOrderItem.getImageUrl())
                    .productPrice(mallOrderItem.getPrice())
                    .productQuantity(mallOrderItem.getQuantity())
                    .productTotalAmount(mallOrderItem.getTotalPrice())
                    .build();
            productInfoLists.add(productInfo);
        });

        // 构建完整的订单详情
        return OrderDetailDto.builder()
                .userInfo(userInfoVo)
                .deliveryInfo(deliveryInfo)
                .orderInfo(orderInfo)
                .productInfo(productInfoLists)
                .build();
    }

    /**
     * 获取订单地址信息
     *
     * @param orderId 订单ID
     * @return 订单地址信息
     */
    @Override
    public OrderAddressVo getOrderAddress(Long orderId) {
        MallOrder mallOrder = getOrderById(orderId);
        return OrderAddressVo.builder()
                .orderId(mallOrder.getId())
                .orderNo(mallOrder.getOrderNo())
                .orderStatus(mallOrder.getOrderStatus())
                .receiverName(mallOrder.getReceiverName())
                .receiverPhone(mallOrder.getReceiverPhone())
                .receiverDetail(mallOrder.getReceiverDetail())
                .deliveryType(mallOrder.getDeliveryType())
                .build();
    }

    /**
     * 更新订单配送信息
     *
     * @param request 更新参数
     * @return 是否更新成功
     */
    @Override
    public boolean updateOrderAddress(AddressUpdateRequest request) {
        // 根据订单号查询订单
        MallOrder mallOrder = getOrderById(request.getOrderId());

        // 检查订单状态是否允许修改地址（只有待支付和待发货状态可以修改地址）
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum != null &&
                orderStatusEnum.ordinal() > OrderStatusEnum.PENDING_SHIPMENT.ordinal()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "当前订单状态不允许修改收货地址");
        }

        // 更新配送信息
        mallOrder.setReceiverName(request.getReceiverName());
        mallOrder.setReceiverPhone(request.getReceiverPhone());
        mallOrder.setReceiverDetail(request.getReceiverAddress());
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(request.getDeliveryType());
        Assert.isTrue(deliveryTypeEnum != null, "配送方式不存在");
        mallOrder.setDeliveryType(deliveryTypeEnum.getType());

        boolean updated = updateById(mallOrder);

        // 添加订单时间线记录
        if (updated) {
            OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                    .orderId(mallOrder.getId())
                    .eventType(OrderEventTypeEnum.OTHER.getType())
                    .eventStatus(mallOrder.getOrderStatus())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .description("管理员修改了收货地址")
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
        }

        return updated;
    }

    /**
     * 获取订单备注信息
     *
     * @param orderId 订单ID
     * @return 订单备注信息
     */
    @Override
    public OrderRemarkVo getOrderRemark(Long orderId) {
        MallOrder mallOrder = getOrderById(orderId);
        return OrderRemarkVo.builder()
                .orderId(mallOrder.getId())
                .orderNo(mallOrder.getOrderNo())
                .remark(mallOrder.getRemark())
                .note(mallOrder.getNote())
                .build();
    }

    /**
     * 更新订单备注
     *
     * @param request 更新参数
     * @return 是否更新成功
     */
    @Override
    public boolean updateOrderRemark(RemarkUpdateRequest request) {
        // 根据订单号查询订单
        MallOrder mallOrder = getOrderById(request.getOrderId());

        // 更新订单备注
        mallOrder.setRemark(request.getRemark());

        boolean updated = updateById(mallOrder);

        // 添加订单时间线记录
        if (updated) {
            OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                    .orderId(mallOrder.getId())
                    .eventType(OrderEventTypeEnum.OTHER.getType())
                    .eventStatus(mallOrder.getOrderStatus())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .description("管理员添加了订单备注")
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
        }

        return updated;
    }

    /**
     * 获取订单价格信息
     *
     * @param orderId 订单ID
     * @return 订单价格信息
     */
    @Override
    public OrderPriceVo getOrderPrice(Long orderId) {
        MallOrder mallOrder = getOrderById(orderId);
        return OrderPriceVo.builder()
                .orderId(mallOrder.getId())
                .orderNo(mallOrder.getOrderNo())
                .totalAmount(mallOrder.getTotalAmount())
                .build();
    }

    /**
     * 更新订单价格
     *
     * @param request 订单价格更新参数
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderPrice(OrderUpdatePriceRequest request) {
        // 根据订单号查询订单
        MallOrder mallOrder = getOrderById(request.getOrderId());

        // 检查订单状态是否允许修改价格（只有待支付状态可以修改价格）
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum != null &&
                orderStatusEnum.ordinal() > OrderStatusEnum.PENDING_PAYMENT.ordinal()) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "当前订单状态不允许修改价格");
        }

        try {
            // 将字符串价格转换为BigDecimal
            BigDecimal newPrice = new BigDecimal(request.getPrice()).setScale(2, RoundingMode.HALF_UP);

            // 验证价格是否合法
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "价格必须大于0");
            }

            // 1. 获取原订单项，计算原总价用于比例分摊
            List<MallOrderItem> items = mallOrderItemService.lambdaQuery()
                    .eq(MallOrderItem::getOrderId, mallOrder.getId())
                    .list();

            if (!CollectionUtils.isEmpty(items)) {
                BigDecimal originalTotalAmount = items.stream()
                        .map(MallOrderItem::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (originalTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal remainingNewPrice = newPrice;
                    for (int i = 0; i < items.size(); i++) {
                        MallOrderItem item = items.get(i);
                        BigDecimal itemNewTotalPrice;

                        if (i == items.size() - 1) {
                            // 最后一个商品，直接取剩余金额，消除舍入误差
                            itemNewTotalPrice = remainingNewPrice;
                        } else {
                            // 按原价占比分摊新价格
                            itemNewTotalPrice = item.getTotalPrice()
                                    .multiply(newPrice)
                                    .divide(originalTotalAmount, 2, RoundingMode.HALF_UP);
                            remainingNewPrice = remainingNewPrice.subtract(itemNewTotalPrice);
                        }

                        item.setTotalPrice(itemNewTotalPrice);
                        // 重新计算单价
                        if (item.getQuantity() != null && item.getQuantity() > 0) {
                            item.setPrice(itemNewTotalPrice.divide(new BigDecimal(item.getQuantity()), 2, RoundingMode.HALF_UP));
                        }
                        item.setUpdateTime(new Date());
                    }
                    // 批量更新订单项
                    mallOrderItemService.updateBatchById(items);
                }
            }

            // 2. 更新订单主表价格
            mallOrder.setTotalAmount(newPrice);
            // 如果是修改总价，通常支付金额也会相应修改
            mallOrder.setPayAmount(newPrice);
            mallOrder.setUpdateTime(new Date());

            boolean updated = updateById(mallOrder);

            // 3. 添加订单时间线记录
            if (updated) {
                OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                        .orderId(mallOrder.getId())
                        .eventType(OrderEventTypeEnum.OTHER.getType())
                        .eventStatus(mallOrder.getOrderStatus())
                        .operatorType(OperatorTypeEnum.ADMIN.getType())
                        .description(String.format("管理员修改了订单价格为: %s", newPrice))
                        .build();
                mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
            }

            return updated;
        } catch (NumberFormatException e) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "价格格式不正确");
        }
    }

    /**
     * 订单退款
     * <p>
     * 整个退款流程必须在事务中执行，确保订单状态更新和钱包充值的原子性。
     * 如果钱包退款成功但订单状态更新失败，会导致重复退款问题。
     * </p>
     *
     * @param request 订单退款参数
     * @return 是否退款成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean orderRefund(OrderRefundRequest request) {
        // 1. 加载并校验订单基本信息与可退款额度，校验失败会直接抛异常并终止流程。
        MallOrder mallOrder = loadRefundableOrder(request);
        PayTypeEnum payType = PayTypeEnum.fromCode(mallOrder.getPayType());
        if (payType == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "暂不支持该支付方式退款!");
        }

        // 2. 先更新订单状态，确保退款金额被记录，防止重复退款
        applyRefundSnapshot(mallOrder, request.getRefundAmount());
        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "更新订单退款状态失败, 请稍后重试!");
        }

        // 3. 根据支付方式路由到具体的退款实现，便于未来扩展到微信、钱包等渠道。
        // 注意：支付宝退款失败会抛异常，触发事务回滚，订单状态会恢复
        switch (payType) {
            case ALIPAY -> processAlipayRefund(mallOrder, request);
            case WALLET -> processWalletRefund(mallOrder, request);
            default -> throw new ServiceException(ResponseCode.OPERATION_ERROR, "暂不支持该支付方式退款!");
        }

        // 4. 添加订单时间线记录
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(mallOrder.getId())
                .eventType(OrderEventTypeEnum.ORDER_REFUNDED.getType())
                .eventStatus(mallOrder.getOrderStatus())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .description("管理员发起订单退款")
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        return true;
    }

    /**
     * 取消订单
     * <p>
     * 取消逻辑：
     * 1. 如果订单未支付：直接取消并恢复库存
     * 2. 如果订单已支付：先全额退款，再取消订单
     * 3. 只有待支付、待发货状态的订单可以取消
     * </p>
     *
     * @param request 订单取消参数
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(OrderCancelRequest request) {
        // 1. 查询订单并校验状态
        MallOrder mallOrder = getOrderById(request.getOrderId());
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());

        if (orderStatusEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }

        // 只有待支付、待发货状态可以取消
        if (orderStatusEnum != OrderStatusEnum.PENDING_PAYMENT &&
                orderStatusEnum != OrderStatusEnum.PENDING_SHIPMENT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许取消", orderStatusEnum.getName()));
        }

        // 3. 如果订单已支付，需要先退款
        if (Objects.equals(mallOrder.getPaid(), PAID_FLAG)) {
            log.info("订单{}已支付，执行全额退款", mallOrder.getOrderNo());

            BigDecimal refundAmount = mallOrder.getPayAmount();
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单支付金额异常，无法退款");
            }

            // 根据支付方式执行退款
            PayTypeEnum payType = PayTypeEnum.fromCode(mallOrder.getPayType());
            if (payType == null) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "支付方式异常");
            }

            switch (payType) {
                case ALIPAY -> // 支付宝退款
                        alipayPaymentService.refund(AlipayRefundRequest.builder()
                                .outTradeNo(mallOrder.getOrderNo())
                                .refundAmount(formatAmount(refundAmount))
                                .refundReason("订单取消-" + (request.getCancelReason() != null ? request.getCancelReason() : "用户取消"))
                                .outRequestNo(buildOutRequestNo(mallOrder))
                                .build());
                case WALLET -> {
                    // 钱包退款
                    Long userId = mallOrder.getUserId();
                    if (userId == null) {
                        throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单用户信息异常");
                    }
                    String walletRemark = String.format("订单取消退款（订单号：%s，退款金额：%s元）",
                            mallOrder.getOrderNo(), formatAmount(refundAmount));
                    boolean success = userWalletService.rechargeWallet(userId, refundAmount, walletRemark);
                    if (!success) {
                        throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包退款失败");
                    }
                }
                default -> throw new ServiceException(ResponseCode.OPERATION_ERROR, "不支持的支付方式");
            }

            // 更新退款信息
            mallOrder.setRefundPrice(refundAmount);
            mallOrder.setRefundTime(new Date());
            mallOrder.setRefundStatus(REFUND_STATUS_SUCCESS);
        }

        // 4. 更新订单状态为已取消
        String cancelReason = request.getCancelReason();
        if (!StringUtils.hasText(cancelReason)) {
            cancelReason = "管理员取消订单";
        }

        mallOrder.setOrderStatus(OrderStatusEnum.CANCELLED.getType());
        mallOrder.setPayType(PayTypeEnum.CANCELLED.getType());
        mallOrder.setCloseReason(cancelReason);
        mallOrder.setCloseTime(new Date());
        mallOrder.setUpdateTime(new Date());

        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "取消订单失败");
        }

        // 5. 恢复库存
        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, mallOrder.getId())
                .list();

        if (!CollectionUtils.isEmpty(orderItems)) {
            for (MallOrderItem orderItem : orderItems) {
                if (orderItem != null && orderItem.getProductId() != null && orderItem.getQuantity() != null) {
                    mallInventoryService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
                    log.info("恢复商品库存，商品ID：{}，数量：{}", orderItem.getProductId(), orderItem.getQuantity());
                }
            }
        }

        // 6. 添加订单时间线记录
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(mallOrder.getId())
                .eventType(OrderEventTypeEnum.ORDER_CANCELLED.getType())
                .eventStatus(OrderStatusEnum.CANCELLED.getType())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .description("管理员取消了订单：" + cancelReason)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        log.info("订单{}取消成功，是否退款：{}", mallOrder.getOrderNo(), Objects.equals(mallOrder.getPaid(), PAID_FLAG));
        return true;
    }

    /**
     * 根据退款请求加载订单并进行前置校验。
     *
     * @param request 退款请求参数
     * @return 可退款且校验通过的订单实体
     */
    private MallOrder loadRefundableOrder(OrderRefundRequest request) {
        // 订单号是定位订单的唯一凭证，这里使用业务异常兜底校验。
        Assert.isTrue(request != null, "订单退款参数不能为空");
        MallOrder mallOrder = getOrderByOrderNo(request.getOrderNo());
        BigDecimal refundAmount = request.getRefundAmount();
        if (refundAmount == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "退款金额不能为空");
        }
        if (!Objects.equals(mallOrder.getPaid(), PAID_FLAG)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单未支付，无法退款!");
        }

        ensureRefundAmountAllowed(mallOrder, refundAmount);
        return mallOrder;
    }

    /**
     * 校验退款金额是否合法：大于 0 且不超过剩余可退金额。
     */
    private void ensureRefundAmountAllowed(MallOrder mallOrder, BigDecimal refundAmount) {
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "退款金额必须大于0!");
        }
        BigDecimal payAmount = safeAmount(mallOrder.getPayAmount());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单支付金额异常，无法退款!");
        }
        BigDecimal alreadyRefunded = safeAmount(mallOrder.getRefundPrice());
        BigDecimal remainingAmount = payAmount.subtract(alreadyRefunded);
        if (refundAmount.compareTo(remainingAmount) > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "退款金额不能大于可退款金额!");
        }
    }

    /**
     * 调用支付宝退款接口，将平台订单号、退款金额等信息传递给网关。
     *
     * @param mallOrder 订单实体
     * @param request   退款请求
     */
    private void processAlipayRefund(MallOrder mallOrder, OrderRefundRequest request) {
        alipayPaymentService.refund(AlipayRefundRequest.builder()
                .outTradeNo(mallOrder.getOrderNo())
                .refundAmount(formatAmount(request.getRefundAmount()))
                .refundReason(determineRefundReason(request.getRefundReason()))
                .outRequestNo(buildOutRequestNo(mallOrder))
                .build());
    }

    /**
     * 钱包退款实现
     * <p>
     * 将退款金额返还到用户钱包余额中，并记录钱包流水
     * </p>
     *
     * @param mallOrder 订单信息
     * @param request   退款请求参数
     */
    private void processWalletRefund(MallOrder mallOrder, OrderRefundRequest request) {
        // 获取退款金额和用户ID
        BigDecimal refundAmount = request.getRefundAmount();
        Long userId = mallOrder.getUserId();

        if (userId == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单用户信息异常，无法退款");
        }

        // 构建退款原因描述
        String walletRemark = String.format("订单退款（订单号：%s，退款原因：%s，退款金额：%s元）",
                mallOrder.getOrderNo(),
                determineRefundReason(request.getRefundReason()),
                formatAmount(refundAmount));

        // 调用钱包服务进行余额充值（退款）
        boolean success = userWalletService.rechargeWallet(userId, refundAmount, walletRemark);

        if (!success) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包退款失败，请稍后重试");
        }

        log.info("钱包退款成功，订单号：{}，用户ID：{}，退款金额：{}",
                mallOrder.getOrderNo(), userId, refundAmount);
    }

    /**
     * 根据本次退款结果刷新订单对象的退款金额、时间与状态，确保后续查询能实时反映退款情况。
     */
    private void applyRefundSnapshot(MallOrder mallOrder, BigDecimal refundAmount) {
        BigDecimal alreadyRefunded = safeAmount(mallOrder.getRefundPrice());
        BigDecimal totalRefunded = alreadyRefunded.add(refundAmount);
        mallOrder.setRefundPrice(totalRefunded);
        mallOrder.setRefundTime(new Date());

        BigDecimal payableAmount = safeAmount(mallOrder.getPayAmount());
        boolean fullyRefunded = payableAmount.compareTo(totalRefunded) == 0;
        mallOrder.setRefundStatus(fullyRefunded ? REFUND_STATUS_SUCCESS : REFUND_STATUS_PARTIAL);

        if (fullyRefunded) {
            mallOrder.setOrderStatus(OrderStatusEnum.REFUNDED.getType());
        } else if (!Objects.equals(mallOrder.getOrderStatus(), OrderStatusEnum.REFUNDED.getType())) {
            mallOrder.setOrderStatus(OrderStatusEnum.AFTER_SALE.getType());
        }
    }

    /**
     * 空值保护，避免金额字段为 null 时触发 NPE。
     */
    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 若未传退款原因则回落到默认文案，方便审计与追责。
     */
    private String determineRefundReason(String refundReason) {
        return StringUtils.hasText(refundReason) ? refundReason : DEFAULT_REFUND_REASON;
    }

    /**
     * 构建支付宝退款的幂等 key，按照「订单号 + 时间戳」规则避免重复退款。
     */
    private String buildOutRequestNo(MallOrder mallOrder) {
        return mallOrder.getOrderNo() + "-REFUND-" + System.currentTimeMillis();
    }

    /**
     * 支付宝退款金额必须保留两位小数，这里统一格式化为字符串。
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public Page<OrderWithProductDto> orderWithProduct(MallOrderListRequest request) {
        Page<OrderWithProductDto> orderWithProductDtoPage = request.toPage();
        Page<OrderWithProductDto> withProductDtoPage = mallOrderMapper.orderListWithProduct(orderWithProductDtoPage, request);
        List<OrderWithProductDto> records = withProductDtoPage.getRecords();
        if (records.isEmpty()) {
            return withProductDtoPage;
        }
        // 获取所有的商品ID
        List<Long> productIds = records.stream()
                .map(OrderWithProductDto::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return withProductDtoPage;
        }
        // 根据商品的ID获取商品的封面图片
        List<MallProductImage> images = mallProductImageService.getFirstImageByProductIds(productIds);
        if (images == null || images.isEmpty()) {
            return withProductDtoPage;
        }
        // 将图片URL映射到商品ID
        Map<Long, String> productImageMap = images.stream()
                .collect(Collectors.toMap(MallProductImage::getProductId, MallProductImage::getImageUrl, (existing, ignore) -> existing));

        // 为每个订单项设置商品图片URL
        records.forEach(orderWithProductDto -> {
            Long productId = orderWithProductDto.getProductId();
            orderWithProductDto.setProductImage(productImageMap.get(productId));
        });
        return withProductDtoPage;
    }

    @Override
    public List<MallOrder> getExpiredOrderClean(long expiredTime) {
        return mallOrderMapper.getExpiredOrderClean(expiredTime);
    }

    @Override
    public Page<MallOrder> getPaidOrderPage(Long userId, PageRequest request) {
        Page<MallOrder> page = request.toPage();
        return mallOrderMapper.getPaidOrderPage(page, userId);
    }

    @Override
    public UserOrderStatistics getOrderStatisticsByUserId(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");
        return userMapper.getOrderStatisticsByUserId(userId);
    }

    /**
     * 获取配送方式描述
     */
    private String getDeliveryTypeDesc(String deliveryType) {
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(deliveryType);
        return deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知";
    }


    /**
     * 获取支付方式描述
     */
    private String getPayTypeDesc(String payType) {
        PayTypeEnum payTypeEnum = PayTypeEnum.fromCode(payType);
        return payTypeEnum != null ? payTypeEnum.getType() : "未知";
    }

    private OrderDetailDto buildOrderDetailDto(OrderDetailRow row) {
        OrderDetailDto.UserInfo userInfo = null;
        if (row.getUserId() != null) {
            userInfo = OrderDetailDto.UserInfo.builder()
                    .userId(String.valueOf(row.getUserId()))
                    .nickname(row.getUserNickname())
                    .phoneNumber(row.getUserPhoneNumber())
                    .build();
        }

        OrderDetailDto.DeliveryInfo deliveryInfo = OrderDetailDto.DeliveryInfo.builder()
                .receiverName(row.getReceiverName())
                .receiverAddress(row.getReceiverDetail())
                .receiverPhone(row.getReceiverPhone())
                .deliveryMethod(getDeliveryTypeDesc(row.getDeliveryType()))
                .build();

        OrderDetailDto.OrderInfo orderInfo = OrderDetailDto.OrderInfo.builder()
                .orderNo(row.getOrderNo())
                .orderStatus(row.getOrderStatus())
                .payType(row.getPayType())
                .totalAmount(row.getTotalAmount())
                .payAmount(row.getPayAmount())
                .freightAmount(row.getFreightAmount())
                .build();

        return OrderDetailDto.builder()
                .userInfo(userInfo)
                .deliveryInfo(deliveryInfo)
                .orderInfo(orderInfo)
                .productInfo(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(OrderShipRequest request) {
        // 1. 查询订单并校验状态
        MallOrder mallOrder = getOrderById(request.getOrderId());

        // 2. 校验订单状态是否允许发货
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }

        // 只有待发货状态可以发货
        if (orderStatusEnum != OrderStatusEnum.PENDING_SHIPMENT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许发货", orderStatusEnum.getName()));
        }

        // 3. 更新订单状态为待收货
        Date now = new Date();
        mallOrder.setOrderStatus(OrderStatusEnum.PENDING_RECEIPT.getType());
        mallOrder.setDeliverTime(now);
        mallOrder.setUpdateTime(now);

        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "发货失败，请重试");
        }

        // 4. 创建物流记录
        MallOrderShipping shipping = MallOrderShipping.builder()
                .orderId(mallOrder.getId())
                .shippingNo(request.getTrackingNumber())
                .shippingCompany(request.getLogisticsCompany())
                .deliveryType(mallOrder.getDeliveryType())
                .status(ShippingStatusEnum.IN_TRANSIT.getType())
                .deliverTime(now)
                .shipmentNote(request.getShipmentNote())
                .createTime(now)
                .updateTime(now)
                .build();

        boolean shippingCreated = mallOrderShippingService.createShipping(shipping);
        if (!shippingCreated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建物流记录失败");
        }

        // 5. 添加订单时间线记录
        String description = String.format("管理员发货，物流公司：%s，物流单号：%s",
                request.getLogisticsCompany(), request.getTrackingNumber());
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(mallOrder.getId())
                .eventType(OrderEventTypeEnum.ORDER_SHIPPED.getType())
                .eventStatus(OrderStatusEnum.PENDING_RECEIPT.getType())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .description(description)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        log.info("订单{}发货成功，物流公司：{}，物流单号：{}", mallOrder.getOrderNo(),
                request.getLogisticsCompany(), request.getTrackingNumber());
        return true;
    }

    @Override
    public OrderShippingVo getOrderShipping(Long orderId) {
        // 1. 查询订单基本信息
        MallOrder mallOrder = getOrderById(orderId);

        // 2. 查询物流信息
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(orderId);

        // 3. 获取订单状态名称
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        String orderStatusName = orderStatusEnum != null ? orderStatusEnum.getName() : "未知";

        // 4. 组装收货人信息
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(mallOrder.getDeliveryType());
        OrderShippingVo.ReceiverInfo receiverInfo = OrderShippingVo.ReceiverInfo.builder()
                .receiverName(mallOrder.getReceiverName())
                .receiverPhone(mallOrder.getReceiverPhone())
                .receiverDetail(mallOrder.getReceiverDetail())
                .deliveryType(mallOrder.getDeliveryType())
                .deliveryTypeName(deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知")
                .build();

        // 5. 组装返回VO
        OrderShippingVo.OrderShippingVoBuilder builder = OrderShippingVo.builder()
                .orderId(mallOrder.getId())
                .orderNo(mallOrder.getOrderNo())
                .orderStatus(mallOrder.getOrderStatus())
                .orderStatusName(orderStatusName)
                .receiverInfo(receiverInfo);

        // 6. 如果有物流信息，添加物流详情
        if (shipping != null) {
            ShippingStatusEnum statusEnum = ShippingStatusEnum.fromCode(shipping.getStatus());
            builder.logisticsCompany(shipping.getShippingCompany())
                    .trackingNumber(shipping.getShippingNo())
                    .shipmentNote(shipping.getShipmentNote())
                    .deliverTime(shipping.getDeliverTime())
                    .receiveTime(shipping.getReceiveTime())
                    .status(shipping.getStatus())
                    .statusName(statusEnum != null ? statusEnum.getName() : "未知");
        }

        return builder.build();
    }

    @Override
    public List<MallOrder> getOrdersForAutoConfirm(int daysAfterShipment) {
        // 计算N天前的时间点
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysAfterShipment);
        Date targetDate = calendar.getTime();

        // 查询发货时间在N天前且状态仍为待收货的订单
        return lambdaQuery()
                .eq(MallOrder::getOrderStatus, OrderStatusEnum.PENDING_RECEIPT.getType())
                .le(MallOrder::getDeliverTime, targetDate)
                .isNotNull(MallOrder::getDeliverTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoConfirmReceipt(Long orderId) {
        // 1. 查询订单并校验状态
        MallOrder mallOrder = getOrderById(orderId);

        // 2. 校验订单状态
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum != OrderStatusEnum.PENDING_RECEIPT) {
            log.warn("订单{}状态不是待收货，无法自动确认收货，当前状态：{}", mallOrder.getOrderNo(), orderStatusEnum);
            return false;
        }

        // 3. 更新订单状态为已完成
        Date now = new Date();
        mallOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getType());
        mallOrder.setReceiveTime(now);
        mallOrder.setFinishTime(now);
        mallOrder.setUpdateTime(now);

        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "自动确认收货失败");
        }

        // 4. 更新物流状态为已签收
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(orderId);
        if (shipping != null) {
            shipping.setStatus(ShippingStatusEnum.DELIVERED.getType());
            shipping.setReceiveTime(now);
            shipping.setUpdateTime(now);
            mallOrderShippingService.updateById(shipping);
        }

        // 5. 添加订单时间线记录（标记为系统自动）
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(mallOrder.getId())
                .eventType(OrderEventTypeEnum.ORDER_RECEIVED.getType())
                .eventStatus(OrderStatusEnum.COMPLETED.getType())
                .operatorType(OperatorTypeEnum.SYSTEM.getType())
                .description("系统自动确认收货")
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        log.info("订单{}自动确认收货成功", mallOrder.getOrderNo());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualConfirmReceipt(OrderReceiveRequest request) {
        // 1. 查询订单并校验
        MallOrder mallOrder = getOrderById(request.getOrderId());

        // 2. 校验订单状态是否允许确认收货
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }

        // 只有待收货状态可以确认收货
        if (orderStatusEnum != OrderStatusEnum.PENDING_RECEIPT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许确认收货", orderStatusEnum.getName()));
        }

        // 3. 更新订单状态为已完成
        Date now = new Date();
        mallOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getType());
        mallOrder.setReceiveTime(now);
        mallOrder.setFinishTime(now);
        mallOrder.setUpdateTime(now);

        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "确认收货失败，请重试");
        }

        // 4. 更新物流状态为已签收
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(request.getOrderId());
        if (shipping != null) {
            shipping.setStatus(ShippingStatusEnum.DELIVERED.getType());
            shipping.setReceiveTime(now);
            shipping.setUpdateTime(now);
            mallOrderShippingService.updateById(shipping);
        }

        // 5. 添加订单时间线记录（标记为管理员操作）
        String username = SecurityUtils.getUsername();
        String description = String.format("管理员%s手动确认收货", username);
        if (request.getRemark() != null && !request.getRemark().trim().isEmpty()) {
            description += String.format("，备注：%s", request.getRemark());
        }

        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(mallOrder.getId())
                .eventType(OrderEventTypeEnum.ORDER_RECEIVED.getType())
                .eventStatus(OrderStatusEnum.COMPLETED.getType())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .description(description)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        log.info("管理员{}手动确认收货成功，订单号：{}，备注：{}", username, mallOrder.getOrderNo(), request.getRemark());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrders(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "请选择要删除的订单");
        }

        List<MallOrder> orders = listByIds(ids);
        if (orders.size() != ids.size()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "存在无效订单ID，无法删除");
        }

        // 当订单状态为 已完成、已取消或已过期时，才允许删除
        for (MallOrder order : orders) {
            String orderStatus = order.getOrderStatus();
            boolean deletable = OrderStatusEnum.COMPLETED.getType().equals(orderStatus)
                    || OrderStatusEnum.CANCELLED.getType().equals(orderStatus)
                    || OrderStatusEnum.EXPIRED.getType().equals(orderStatus);
            if (!deletable) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "只有订单状态为已完成、已取消或已过期才能被删除!");
            }
        }

        boolean deleted = removeByIds(ids);
        if (!deleted) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "删除订单失败");
        }

        orders.forEach(order -> log.info("订单{}删除成功", order.getOrderNo()));
        return true;
    }

    @Override
    public List<OrderDetailDto> getOrderDetailByIds(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return List.of();
        }

        // 1. 批量查询订单
        List<MallOrder> orders = listByIds(orderIds);
        if (orders.isEmpty()) {
            return List.of();
        }

        // 2. 批量查询用户信息
        List<Long> userIds = orders.stream()
                .map(MallOrder::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user, (existing, ignore) -> existing));
        }

        // 3. 批量查询订单商品
        List<Long> orderIdsFromOrders = orders.stream().map(MallOrder::getId).toList();
        List<MallOrderItem> allOrderItems = mallOrderItemService.lambdaQuery()
                .in(MallOrderItem::getOrderId, orderIdsFromOrders)
                .list();
        Map<Long, List<MallOrderItem>> orderItemMap = allOrderItems.stream()
                .collect(Collectors.groupingBy(MallOrderItem::getOrderId));

        // 4. 构建返回结果
        List<OrderDetailDto> result = new ArrayList<>();
        for (MallOrder mallOrder : orders) {
            // 获取用户信息
            User userInfo = userMap.get(mallOrder.getUserId());
            OrderDetailDto.UserInfo userInfoVo = null;
            if (userInfo != null) {
                userInfoVo = OrderDetailDto.UserInfo.builder()
                        .userId(userInfo.getId().toString())
                        .nickname(userInfo.getNickname())
                        .phoneNumber(userInfo.getPhoneNumber())
                        .build();
            }

            // 构建配送信息
            OrderDetailDto.DeliveryInfo deliveryInfo = OrderDetailDto.DeliveryInfo.builder()
                    .receiverName(mallOrder.getReceiverName())
                    .receiverAddress(mallOrder.getReceiverDetail())
                    .receiverPhone(mallOrder.getReceiverPhone())
                    .deliveryMethod(getDeliveryTypeDesc(mallOrder.getDeliveryType()))
                    .build();

            // 构建订单信息
            OrderDetailDto.OrderInfo orderInfo = OrderDetailDto.OrderInfo.builder()
                    .orderNo(mallOrder.getOrderNo())
                    .orderStatus(mallOrder.getOrderStatus())
                    .payType(mallOrder.getPayType())
                    .totalAmount(mallOrder.getTotalAmount())
                    .payAmount(mallOrder.getPayAmount())
                    .freightAmount(mallOrder.getFreightAmount())
                    .build();

            // 构建商品信息
            List<OrderDetailDto.ProductInfo> productInfoList = new ArrayList<>();
            List<MallOrderItem> orderItems = orderItemMap.getOrDefault(mallOrder.getId(), List.of());
            for (MallOrderItem item : orderItems) {
                OrderDetailDto.ProductInfo productInfo = OrderDetailDto.ProductInfo.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productImage(item.getImageUrl())
                        .productPrice(item.getPrice())
                        .productQuantity(item.getQuantity())
                        .productTotalAmount(item.getTotalPrice())
                        .build();
                productInfoList.add(productInfo);
            }

            result.add(OrderDetailDto.builder()
                    .userInfo(userInfoVo)
                    .deliveryInfo(deliveryInfo)
                    .orderInfo(orderInfo)
                    .productInfo(productInfoList)
                    .build());
        }

        return result;
    }

}
