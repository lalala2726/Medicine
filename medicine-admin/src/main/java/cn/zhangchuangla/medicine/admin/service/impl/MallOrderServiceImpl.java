package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.admin.mapper.UserMapper;
import cn.zhangchuangla.medicine.admin.model.dto.UserOrderStatistics;
import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.dto.OrderTimelineDto;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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


    public MallOrderServiceImpl(MallOrderMapper mallOrderMapper, UserMapper userMapper, MallOrderItemService mallOrderItemService, MallProductImageService mallProductImageService, AlipayPaymentService alipayPaymentService, MallOrderTimelineService mallOrderTimelineService, UserWalletService userWalletService) {
        this.mallOrderMapper = mallOrderMapper;
        this.userMapper = userMapper;
        this.mallOrderItemService = mallOrderItemService;
        this.mallProductImageService = mallProductImageService;
        this.alipayPaymentService = alipayPaymentService;
        this.mallOrderTimelineService = mallOrderTimelineService;
        this.userWalletService = userWalletService;
    }


    @Override
    public Page<MallOrder> orderList(MallOrderListRequest request) {
        Page<MallOrder> mallOrderPage = request.toPage();
        return mallOrderMapper.orderList(mallOrderPage, request);
    }

    @Override
    public MallOrder getOrderByOrderNo(String orderNo) {
        Assert.isTrue(orderNo != null, "订单号不能为空");
        MallOrder mallOrder = lambdaQuery().eq(MallOrder::getOrderNo, orderNo).one();
        if (mallOrder == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "订单不存在");
        }
        return mallOrder;
    }

    @Override
    public MallOrder getOrderById(Long id) {
        Assert.isPositive(id, "订单ID不能小于0");
        MallOrder mallOrder = getById(id);
        if (mallOrder == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "订单不存在");
        }
        return mallOrder;
    }

    @Override
    public OrderDetailVo orderDetail(Long orderId) {

        // 获取订单信息
        MallOrder mallOrder = getOrderById(orderId);
        // 获取用户信息
        User userInfo = userMapper.selectById(mallOrder.getUserId());
        // 获取商品信息
        List<MallOrderItem> mallOrderItems = mallOrderItemService.getOrderItemByOrderId(mallOrder.getId());

        // 构建用户信息
        OrderDetailVo.UserInfo userInfoVo = OrderDetailVo.UserInfo.builder()
                .userId(userInfo.getId().toString())
                .nickname(userInfo.getNickname())
                .phoneNumber(userInfo.getPhoneNumber())
                .build();

        // 构建配送信息
        OrderDetailVo.DeliveryInfo deliveryInfo = OrderDetailVo.DeliveryInfo.builder()
                .receiverName(mallOrder.getReceiverName())
                .receiverAddress(mallOrder.getReceiverDetail())
                .receiverPhone(mallOrder.getReceiverPhone())
                .deliveryMethod(getDeliveryTypeDesc(mallOrder.getDeliveryType()))
                .build();

        // 构建订单信息
        OrderDetailVo.OrderInfo orderInfo = OrderDetailVo.OrderInfo.builder()
                .orderNo(mallOrder.getOrderNo())
                .orderStatus(getOrderStatusDesc(mallOrder.getOrderStatus()))
                .payType(getPayTypeDesc(mallOrder.getPayType()))
                .totalAmount(mallOrder.getTotalAmount())
                .payAmount(mallOrder.getPayAmount())
                .freightAmount(mallOrder.getFreightAmount())
                .build();

        // 构建商品信息
        List<OrderDetailVo.ProductInfo> productInfoLists = new ArrayList<>();
        mallOrderItems.forEach(mallOrderItem -> {
            OrderDetailVo.ProductInfo productInfo = OrderDetailVo.ProductInfo.builder()
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
        return OrderDetailVo.builder()
                .userInfo(userInfoVo)
                .deliveryInfo(deliveryInfo)
                .orderInfo(orderInfo)
                .productInfo(productInfoLists)
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
            throw new ServiceException(ResponseResultCode.PARAM_ERROR, "当前订单状态不允许修改收货地址");
        }

        // 更新配送信息
        mallOrder.setReceiverName(request.getReceiverName());
        mallOrder.setReceiverPhone(request.getReceiverPhone());
        mallOrder.setReceiverDetail(request.getReceiverDetail());
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(request.getDeliveryType());
        Assert.isTrue(deliveryTypeEnum != null, "配送方式不存在");
        mallOrder.setDeliveryType(deliveryTypeEnum.getType());

        boolean updated = updateById(mallOrder);

        // 添加订单时间线记录
        if (updated) {
            OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                    .orderId(mallOrder.getId())
                    .eventType(OrderEventTypeEnum.ADMIN_UPDATE_ADDRESS.getType())
                    .eventStatus(mallOrder.getOrderStatus())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .description("管理员修改了收货地址")
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
        }

        return updated;
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
                    .eventType(OrderEventTypeEnum.ADMIN_UPDATE_REMARK.getType())
                    .eventStatus(mallOrder.getOrderStatus())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .description("管理员添加了订单备注")
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
        }

        return updated;
    }

    /**
     * 更新订单价格
     *
     * @param request 订单价格更新参数
     * @return 是否更新成功
     */
    @Override
    public boolean updateOrderPrice(OrderUpdatePriceRequest request) {
        // 根据订单号查询订单
        MallOrder mallOrder = getOrderById(request.getOrderId());

        // 检查订单状态是否允许修改价格（只有待支付状态可以修改价格）
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum != null &&
                orderStatusEnum.ordinal() > OrderStatusEnum.PENDING_PAYMENT.ordinal()) {
            throw new ServiceException(ResponseResultCode.PARAM_ERROR, "当前订单状态不允许修改价格");
        }

        try {
            // 将字符串价格转换为BigDecimal
            BigDecimal newPrice = new BigDecimal(request.getPrice());

            // 验证价格是否合法
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(ResponseResultCode.PARAM_ERROR, "价格必须大于0");
            }

            // 更新订单价格
            mallOrder.setTotalAmount(newPrice);
            // 如果是修改总价，通常支付金额也会相应修改
            mallOrder.setPayAmount(newPrice);

            boolean updated = updateById(mallOrder);

            // 添加订单时间线记录
            if (updated) {
                OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                        .orderId(mallOrder.getId())
                        .eventType(OrderEventTypeEnum.ADMIN_UPDATE_PRICE.getType())
                        .eventStatus(mallOrder.getOrderStatus())
                        .operatorType(OperatorTypeEnum.ADMIN.getType())
                        .description("管理员修改了订单价格")
                        .build();
                mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
            }

            return updated;
        } catch (NumberFormatException e) {
            throw new ServiceException(ResponseResultCode.PARAM_ERROR, "价格格式不正确");
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
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "暂不支持该支付方式退款!");
        }

        // 2. 先更新订单状态，确保退款金额被记录，防止重复退款
        applyRefundSnapshot(mallOrder, request.getRefundAmount());
        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "更新订单退款状态失败, 请稍后重试!");
        }

        // 3. 根据支付方式路由到具体的退款实现，便于未来扩展到微信、钱包等渠道。
        // 注意：支付宝退款失败会抛异常，触发事务回滚，订单状态会恢复
        switch (payType) {
            case ALIPAY -> processAlipayRefund(mallOrder, request);
            case WALLET -> processWalletRefund(mallOrder, request);
            default -> throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "暂不支持该支付方式退款!");
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
            throw new ServiceException(ResponseResultCode.PARAM_ERROR, "退款金额不能为空");
        }
        if (!Objects.equals(mallOrder.getPaid(), PAID_FLAG)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "订单未支付，无法退款!");
        }

        ensureRefundAmountAllowed(mallOrder, refundAmount);
        return mallOrder;
    }

    /**
     * 校验退款金额是否合法：大于 0 且不超过剩余可退金额。
     */
    private void ensureRefundAmountAllowed(MallOrder mallOrder, BigDecimal refundAmount) {
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "退款金额必须大于0!");
        }
        BigDecimal payAmount = safeAmount(mallOrder.getPayAmount());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "订单支付金额异常，无法退款!");
        }
        BigDecimal alreadyRefunded = safeAmount(mallOrder.getRefundPrice());
        BigDecimal remainingAmount = payAmount.subtract(alreadyRefunded);
        if (refundAmount.compareTo(remainingAmount) > 0) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "退款金额不能大于可退款金额!");
        }
    }

    /**
     * 调用支付宝退款接口，将平台订单号、退款金额等信息传递给网关。
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
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "订单用户信息异常，无法退款");
        }

        // 构建退款原因描述
        String walletRemark = String.format("订单退款（订单号：%s，退款原因：%s，退款金额：%s元）",
                mallOrder.getOrderNo(),
                determineRefundReason(request.getRefundReason()),
                formatAmount(refundAmount));

        // 调用钱包服务进行余额充值（退款）
        boolean success = userWalletService.rechargeWallet(userId, refundAmount, walletRemark);

        if (!success) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "钱包退款失败，请稍后重试");
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
     * 获取订单状态描述
     */
    private String getOrderStatusDesc(String orderStatus) {
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(orderStatus);
        return orderStatusEnum != null ? orderStatusEnum.getName() : "未知";
    }

    /**
     * 获取支付方式描述
     */
    private String getPayTypeDesc(String payType) {
        PayTypeEnum payTypeEnum = PayTypeEnum.fromCode(payType);
        return payTypeEnum != null ? payTypeEnum.getType() : "未知";
    }
}
