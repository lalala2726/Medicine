package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.elasticsearch.document.MallProductDocument;
import cn.zhangchuangla.medicine.client.elasticsearch.service.MallProductSearchService;
import cn.zhangchuangla.medicine.client.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.client.model.dto.MallOrderDto;
import cn.zhangchuangla.medicine.client.model.dto.OrderDetailDto;
import cn.zhangchuangla.medicine.client.model.request.*;
import cn.zhangchuangla.medicine.client.model.vo.*;
import cn.zhangchuangla.medicine.client.service.*;
import cn.zhangchuangla.medicine.client.task.OrderDelayProducer;
import cn.zhangchuangla.medicine.common.core.constants.RedisConstants;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.redis.core.RedisCache;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.*;
import cn.zhangchuangla.medicine.model.entity.*;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.model.vo.mall.OrderShippingVo;
import cn.zhangchuangla.medicine.payment.config.AlipayProperties;
import cn.zhangchuangla.medicine.payment.model.AlipayPagePayRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.zhangchuangla.medicine.common.core.constants.Constants.ORDER_TIMEOUT_MINUTES;
import static cn.zhangchuangla.medicine.model.enums.PayTypeEnum.ALIPAY;

/**
 * @author Chuang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService, BaseService {

    private static final String ORDER_STATUS_WAIT_PAY = OrderStatusEnum.PENDING_PAYMENT.getType();
    private static final String ORDER_STATUS_WAIT_SHIPMENT = OrderStatusEnum.PENDING_SHIPMENT.getType();
    private static final String PAY_TYPE_ALIPAY = ALIPAY.getType();
    private static final String WAIT_PAY = PayTypeEnum.WAIT_PAY.getType();
    private static final int SALES_SYNC_THRESHOLD = 5;

    private final MallProductService mallProductService;
    private final MallOrderItemService mallOrderItemService;
    private final AlipayPaymentService alipayPaymentService;
    private final AlipayProperties alipayProperties;
    private final OrderDelayProducer orderDelayProducer;
    private final UserWalletService userWalletService;
    private final MallOrderTimelineService mallOrderTimelineService;
    private final MallOrderShippingService mallOrderShippingService;
    private final MallCartService mallCartService;
    private final UserAddressService userAddressService;
    private final RedisCache redisCache;
    private final MallProductSearchService mallProductSearchService;


    /**
     * 提交订单（创建订单并锁定库存）
     * <p>
     * 用户提交订单时创建订单并扣减库存，订单状态为待支付。
     * 订单创建后需要在30分钟内完成支付，否则订单将自动取消并恢复库存。
     * </p>
     *
     * @param request 订单提交请求参数
     * @return 订单提交结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCheckoutVo checkoutOrder(OrderCheckoutRequest request) {
        // 1. 查询商品详情并校验上架状态
        MallProductWithImageDto mallProductWithImageDto = mallProductService.getProductWithImagesById(request.getProductId());

        if (mallProductWithImageDto == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }

        // 校验商品状态
        final Integer PRODUCT_STATUS_ON_SALE = 1;
        if (!Objects.equals(mallProductWithImageDto.getStatus(), PRODUCT_STATUS_ON_SALE)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品未上架或已下架");
        }

        // 校验库存
        Integer stock = mallProductWithImageDto.getStock();
        if (stock == null || stock < request.getQuantity()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("商品库存不足，当前库存：%d", stock == null ? 0 : stock));
        }

        // 计算订单总金额
        BigDecimal price = mallProductWithImageDto.getPrice();
        if (price == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品价格未配置");
        }
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        // 2. 扣减库存，内部包含乐观锁控制
        mallProductService.deductStock(request.getProductId(), request.getQuantity());

        // 3. 生成业务订单号并补充订单基础信息
        String orderNo = generateOrderNo();
        Date now = new Date();

        // 使用用户选择的配送方式
        String deliveryTypeCode = request.getDeliveryType().getType();
        Long userId = SecurityUtils.getUserId();
        UserAddress userAddress = getUserAddressOrThrow(userId, request.getAddressId());
        String receiverDetail = buildReceiverDetail(userAddress);

        MallOrder order = MallOrder.builder()
                .orderNo(orderNo)
                .userId(userId)
                .totalAmount(totalAmount)
                .payAmount(BigDecimal.ZERO)
                .freightAmount(BigDecimal.ZERO)
                .payType(WAIT_PAY)
                .orderStatus(ORDER_STATUS_WAIT_PAY)
                .deliveryType(deliveryTypeCode)
                .addressId(userAddress.getId())
                .receiverName(userAddress.getReceiverName())
                .receiverPhone(userAddress.getReceiverPhone())
                .receiverDetail(receiverDetail)
                .note(request.getRemark())
                .payExpireTime(DateUtils.addMinutes(now, ORDER_TIMEOUT_MINUTES))
                .afterSaleFlag(OrderItemAfterSaleStatusEnum.NONE)
                .createTime(now)
                .updateTime(now)
                .build();

        // 4. 先保存订单
        if (!save(order)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建订单失败，请稍后再试");
        }

        MallProductImage mallProductImage = null;
        if (mallProductWithImageDto.getProductImages() != null && !mallProductWithImageDto.getProductImages().isEmpty()) {
            mallProductImage = mallProductWithImageDto.getProductImages().getFirst();
        }

        MallOrderItem mallOrderItem = MallOrderItem.builder()
                .orderId(order.getId())
                .productId(mallProductWithImageDto.getId())
                .productName(mallProductWithImageDto.getName())
                .quantity(request.getQuantity())
                .price(mallProductWithImageDto.getPrice())
                .imageUrl(mallProductImage == null ? "" : mallProductImage.getImageUrl())
                .totalPrice(totalAmount)
                .createTime(now)
                .updateTime(now)
                .build();

        // 5. 保存订单项
        if (!mallOrderItemService.save(mallOrderItem)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "创建订单失败，请稍后再试");
        }

        Date expireTime = Date.from(LocalDateTime.now()
                .plusMinutes(ORDER_TIMEOUT_MINUTES)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        // 设置订单定时关闭
        orderDelayProducer.addOrderToDelayQueue(orderNo, ORDER_TIMEOUT_MINUTES);

        // 添加订单创建时间线记录
        String username = getUsername();
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(order.getId())
                .eventType(OrderEventTypeEnum.ORDER_CREATED.getType())
                .eventStatus(order.getOrderStatus())
                .operatorType(OperatorTypeEnum.USER.getType())
                .description(String.format("用户%s创建了订单", username))
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        return OrderCheckoutVo.builder()
                .orderNo(orderNo)
                .totalAmount(totalAmount)
                .orderStatus(ORDER_STATUS_WAIT_PAY)
                .createTime(now)
                .payExpireTime(expireTime)
                .productSummary(buildProductSummary(mallProductWithImageDto, request.getQuantity()))
                .itemCount(1)
                .build();
    }

    @Override
    public OrderPayInfoVo getOrderPayInfo(OrderPayInfoRequest request) {
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, request.getOrderNo())
                .one();

        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        Long userId = getUserId();
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在!");
        }

        if (!Objects.equals(order.getOrderStatus(), ORDER_STATUS_WAIT_PAY)) {
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(order.getOrderStatus());
            String statusName = orderStatusEnum != null ? orderStatusEnum.getName() : "未知";
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不支持支付", statusName));
        }

        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, order.getId())
                .orderByAsc(MallOrderItem::getId)
                .list();

        Date payExpireTime = order.getPayExpireTime();
        if (payExpireTime == null && order.getCreateTime() != null) {
            payExpireTime = DateUtils.addMinutes(order.getCreateTime(), ORDER_TIMEOUT_MINUTES);
        }

        return OrderPayInfoVo.builder()
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .createTime(order.getCreateTime())
                .payExpireTime(payExpireTime)
                .productSummary(buildProductSummary(orderItems))
                .itemCount(orderItems == null ? 0 : orderItems.size())
                .build();
    }

    /**
     * 订单支付
     * <p>
     * 对已创建的待支付订单进行支付操作，支持钱包支付和支付宝支付：
     * - 钱包支付：同步扣款，订单状态变为待发货
     * - 支付宝支付：生成支付表单，订单状态保持待支付，等待异步回调
     * </p>
     *
     * @param request 订单支付请求参数
     * @return 订单支付结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPayVo payOrder(OrderPayRequest request) {
        // 1. 查询订单并校验状态
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, request.getOrderNo())
                .one();

        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 2. 校验订单所属用户
        Long orderUserId = order.getUserId();
        Long userId = getUserId();
        if (!Objects.equals(orderUserId, userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在!");
        }

        // 3. 校验订单状态（必须是待支付状态）
        if (!Objects.equals(order.getOrderStatus(), ORDER_STATUS_WAIT_PAY)) {
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(order.getOrderStatus());
            String statusName = orderStatusEnum != null ? orderStatusEnum.getName() : "未知";
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许支付", statusName));
        }

        // 4. 根据支付方式处理支付
        String paymentStatus;
        String paymentData = null;
        String finalOrderStatus = order.getOrderStatus();
        BigDecimal payAmount = order.getTotalAmount();

        switch (request.getPayMethod()) {
            case WALLET -> {
                // 钱包支付：同步扣款
                boolean result = userWalletService.deductBalance(userId, payAmount,
                        String.format("订单支付-%s", request.getOrderNo()));
                if (result) {
                    markOrderPaid(request.getOrderNo(), payAmount, PayTypeEnum.WALLET.getType());
                    paymentStatus = "SUCCESS";
                    finalOrderStatus = ORDER_STATUS_WAIT_SHIPMENT;
                } else {
                    throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包余额不足");
                }
            }
            case ALIPAY -> {
                // 支付宝支付：生成支付表单
                paymentData = alipayPay(order);
                paymentStatus = "PENDING";
            }
            default -> throw new ServiceException(ResponseCode.OPERATION_ERROR, "不支持的支付方式");
        }

        return OrderPayVo.builder()
                .orderNo(request.getOrderNo())
                .payAmount(payAmount)
                .orderStatus(finalOrderStatus)
                .paymentMethod(request.getPayMethod().getType())
                .paymentStatus(paymentStatus)
                .paymentData(paymentData)
                .build();
    }

    /**
     * 支付宝支付
     * <p>
     * 该方法用于生成支付宝页面支付表单，供用户完成支付操作。
     * 主要逻辑包括：校验订单金额、构造商品描述信息、设置支付参数并生成支付表单。
     *
     * @param order 商城订单对象，包含订单的基本信息如订单号、总金额等
     * @return 返回支付宝页面支付表单的HTML字符串，用于前端展示支付页面
     * @throws ServiceException 当订单金额为空时抛出业务异常
     */
    private String alipayPay(MallOrder order) {
        // 校验订单金额是否有效
        BigDecimal amount = order.getTotalAmount();
        if (amount == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单金额缺失，无法发起支付");
        }

        // 查询订单的第一个商品项，用于构造支付主题
        MallOrderItem firstItem = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, order.getId())
                .orderByAsc(MallOrderItem::getId)
                .last("LIMIT 1")
                .one();

        // 构造支付主题：优先使用第一个商品名称，否则使用默认格式
        String subject;
        if (firstItem != null && StringUtils.hasText(firstItem.getProductName())) {
            subject = firstItem.getProductName();
            Integer quantity = firstItem.getQuantity();
            if (quantity != null && quantity > 0) {
                subject = subject + " x" + quantity;
            }
        } else {
            subject = "商城订单-" + order.getOrderNo();
        }

        // 格式化金额为保留两位小数的字符串形式
        String totalAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();

        // 获取回调地址配置
        String notifyUrl = alipayProperties.getNotifyUrl();
        String returnUrl = alipayProperties.getReturnUrl();
        log.info("构建支付宝页面支付表单，orderNo={}，notifyUrl={}，returnUrl={}", order.getOrderNo(), notifyUrl, returnUrl);

        // 构建支付宝支付请求参数
        AlipayPagePayRequest payRequest = AlipayPagePayRequest.builder()
                .outTradeNo(order.getOrderNo())
                .subject(subject)
                .totalAmount(totalAmount)
                .body("商城订单支付（订单号：" + order.getOrderNo() + "）")
                .timeoutExpress(ORDER_TIMEOUT_MINUTES + "m")
                .notifyUrl(notifyUrl)
                .returnUrl(returnUrl)
                .build();

        // 调用服务生成支付表单并返回
        return alipayPaymentService.generatePagePayForm(payRequest);
    }


    /**
     * 标记订单为已支付（支付宝支付）
     *
     * @param orderNo   订单号
     * @param payAmount 支付金额
     * @return 是否更新成功
     */
    private boolean markOrderPaidByAlipay(String orderNo, BigDecimal payAmount) {
        return markOrderPaid(orderNo, payAmount, PAY_TYPE_ALIPAY);
    }

    /**
     * 标记订单为已支付（通用方法）
     * <p>
     * 更新订单状态为待发货，设置支付方式、支付金额、支付时间等信息，并添加时间线记录
     * </p>
     *
     * @param orderNo   订单号
     * @param payAmount 支付金额
     * @param payType   支付方式
     * @return 是否更新成功
     */
    private boolean markOrderPaid(String orderNo, BigDecimal payAmount, String payType) {
        final int PAID = 1;

        // 查询订单信息
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("订单不存在，订单号：{}", orderNo);
            return false;
        }

        // 如果订单状态不是待支付，说明已经处理过了
        if (!Objects.equals(order.getOrderStatus(), ORDER_STATUS_WAIT_PAY)) {
            log.info("订单状态不是待支付，跳过处理，订单号：{}，当前状态：{}", orderNo, order.getOrderStatus());
            return true;
        }

        // 计算最终支付金额
        Date now = new Date();
        BigDecimal finalPayAmount = payAmount != null ? payAmount : order.getTotalAmount();
        log.info("订单支付成功，订单号：{}，支付方式：{}，支付金额：{}", orderNo, payType, finalPayAmount);

        // 更新订单状态
        boolean updated = lambdaUpdate()
                .eq(MallOrder::getId, order.getId())
                .set(MallOrder::getOrderStatus, ORDER_STATUS_WAIT_SHIPMENT)
                .set(MallOrder::getPayType, payType)
                .set(MallOrder::getPayAmount, finalPayAmount)
                .set(MallOrder::getPayTime, now)
                .set(MallOrder::getUpdateTime, now)
                .set(MallOrder::getPaid, PAID)
                .update();

        // 添加订单支付时间线记录
        if (updated) {
            // todo 这边需要从订单信息拿到用户的信息
            OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                    .orderId(order.getId())
                    .eventType(OrderEventTypeEnum.ORDER_PAID.getType())
                    .eventStatus(ORDER_STATUS_WAIT_SHIPMENT)
                    .operatorType(OperatorTypeEnum.USER.getType())
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
        }

        return updated;
    }

    /**
     * 支付宝异步通知回调处理逻辑
     *
     * @param alipayNotifyDTO 支付宝异步通知参数
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String alipayNotify(AlipayNotifyDTO alipayNotifyDTO, HttpServletRequest request) {

        final String SUCCESS_RESPONSE = "success";
        final String FAILURE_RESPONSE = "failure";

        log.info("收到支付宝异步通知，requestURI={}，remoteAddr={}，paramCount={}",
                request.getRequestURI(), request.getRemoteAddr(), request.getParameterMap().size());

        if (alipayNotifyDTO == null) {
            log.warn("收到空的支付宝异步通知");
            return FAILURE_RESPONSE;
        }

        log.info("支付宝通知核心字段，outTradeNo={}，tradeStatus={}，notifyId={}",
                alipayNotifyDTO.getOut_trade_no(), alipayNotifyDTO.getTrade_status(), alipayNotifyDTO.getNotify_id());

        Map<String, String> params = buildNotifyParamMap(request, alipayNotifyDTO);
        if (params.isEmpty()) {
            log.warn("支付宝异步通知参数为空，订单号：{}", alipayNotifyDTO.getOut_trade_no());
            return FAILURE_RESPONSE;
        }

        log.debug("支付宝异步通知完整参数，orderNo={}，params={}", alipayNotifyDTO.getOut_trade_no(), params);

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                log.warn("支付宝异步通知验签失败，订单号：{}，参数：{}", alipayNotifyDTO.getOut_trade_no(), params);
                return FAILURE_RESPONSE;
            }
        } catch (AlipayApiException ex) {
            log.error("支付宝异步通知验签异常，订单号：{}", alipayNotifyDTO.getOut_trade_no(), ex);
            return FAILURE_RESPONSE;
        }

        String tradeStatus = alipayNotifyDTO.getTrade_status();
        String orderNo = alipayNotifyDTO.getOut_trade_no();
        log.info("支付宝异步通知到达，订单号：{}，交易状态：{}", orderNo, tradeStatus);

        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.info("支付宝通知状态非支付成功，订单号：{}，状态：{}，忽略更新", orderNo, tradeStatus);
            return SUCCESS_RESPONSE;
        }

        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("支付宝通知对应订单不存在，订单号：{}", orderNo);
            return FAILURE_RESPONSE;
        }

        BigDecimal notifyAmount = parseAmount(alipayNotifyDTO.getTotal_amount());
        if (!isPayAmountMatched(order, notifyAmount)) {
            log.warn("支付宝通知金额与订单不符，订单号：{}，订单金额：{}，回调金额：{}", orderNo, order.getTotalAmount(), notifyAmount);
            return FAILURE_RESPONSE;
        }

        boolean updated = markOrderPaidByAlipay(orderNo, notifyAmount);
        if (!updated) {
            log.warn("标记订单支付状态失败，订单号：{}", orderNo);
            return FAILURE_RESPONSE;
        }

        log.info("订单支付宝支付完成，已更新状态，订单号：{}", orderNo);
        return SUCCESS_RESPONSE;
    }

    @Override
    public void closeOrderIfUnpaid(String orderNo) {
        log.info("订单 {} 未支付，准备关闭", orderNo);

        // 先查询订单当前版本和状态
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, orderNo)
                .eq(MallOrder::getOrderStatus, ORDER_STATUS_WAIT_PAY)
                .select(MallOrder::getId, MallOrder::getVersion)
                .one();

        if (order == null) {
            log.info("订单 {} 未执行关闭，当前状态可能已变更", orderNo);
            return;
        }

        // 查询订单项，用于恢复库存
        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, order.getId())
                .list();

        // 使用版本号进行乐观锁更新
        boolean updated = lambdaUpdate()
                .eq(MallOrder::getId, order.getId())
                .eq(MallOrder::getVersion, order.getVersion())
                .set(MallOrder::getOrderStatus, OrderStatusEnum.EXPIRED.getType())
                .set(MallOrder::getCloseReason, "订单支付超时，系统自动关闭")
                .set(MallOrder::getCloseTime, new Date())
                .set(MallOrder::getUpdateBy, "系统自动关闭")
                .set(MallOrder::getUpdateTime, new Date())
                .update();

        if (updated) {
            log.info("订单 {} 已自动关闭", orderNo);
            // 恢复库存
            if (orderItems != null) {
                for (MallOrderItem orderItem : orderItems) {
                    if (orderItem != null && orderItem.getProductId() != null && orderItem.getQuantity() != null) {
                        mallProductService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
                    }
                }
            }

            // 添加订单过期时间线记录
            OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                    .orderId(order.getId())
                    .eventType(OrderEventTypeEnum.ORDER_EXPIRED.getType())
                    .eventStatus(OrderStatusEnum.EXPIRED.getType())
                    .operatorType(OperatorTypeEnum.SYSTEM.getType())
                    .description("订单支付超时，系统自动关闭")
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(timelineDto);
        } else {
            log.info("订单 {} 未执行关闭，当前状态可能已变更", orderNo);
        }
    }

    /**
     * 组装商品摘要，方便前端展示订单信息。
     */
    private String buildProductSummary(MallProduct product, int quantity) {
        String unit = product.getUnit();
        if (unit != null && !unit.isBlank()) {
            return product.getName() + " " + quantity + unit;
        }
        return product.getName() + " x" + quantity;
    }

    /**
     * 根据订单项构建商品摘要。
     */
    private String buildProductSummary(List<MallOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "订单商品";
        }
        MallOrderItem first = items.getFirst();
        String name = StringUtils.hasText(first.getProductName()) ? first.getProductName() : "商品";
        Integer quantity = first.getQuantity();
        String base = quantity != null && quantity > 0 ? name + " x" + quantity : name;
        if (items.size() > 1) {
            base = base + " 等" + items.size() + "件";
        }
        return base;
    }


    /**
     * 生成业务唯一的订单编号。
     */
    private String generateOrderNo() {
        String prefix = "O";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%06d", (int) (Math.random() * 1000000));
        return prefix + datePart + randomPart;
    }

    /**
     * 构建支付宝回调参数 Map，用于验签。
     */
    private Map<String, String> buildNotifyParamMap(HttpServletRequest request, AlipayNotifyDTO notifyDTO) {
        Map<String, String> params = new HashMap<>();
        if (request != null) {
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    params.put(key, values[0]);
                }
            });
        }
        if (params.isEmpty() && notifyDTO != null) {
            for (Field field : AlipayNotifyDTO.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(notifyDTO);
                    if (value != null) {
                        params.put(field.getName(), value.toString());
                    }
                } catch (IllegalAccessException ex) {
                    log.debug("读取支付宝通知字段失败：{}", field.getName(), ex);
                }
            }
        }
        return params;
    }

    /**
     * 解析字符串金额为 BigDecimal。
     */
    private BigDecimal parseAmount(String amountStr) {
        if (!StringUtils.hasText(amountStr)) {
            return null;
        }
        try {
            return new BigDecimal(amountStr);
        } catch (NumberFormatException ex) {
            log.warn("支付宝回调金额解析失败，原始金额：{}", amountStr);
            return null;
        }
    }

    /**
     * 校验订单应付金额与支付宝回调金额是否一致。
     */
    private boolean isPayAmountMatched(MallOrder order, BigDecimal payAmount) {
        if (order == null) {
            return false;
        }
        BigDecimal orderAmount = order.getTotalAmount();
        if (orderAmount == null || payAmount == null) {
            return false;
        }
        return orderAmount.compareTo(payAmount) == 0;
    }

    /**
     * 用户取消订单
     * <p>
     * 用户主动取消订单，需要提供取消原因。
     * 只有待支付状态的订单可以取消，取消后会恢复库存。
     * </p>
     *
     * @param request 订单取消请求参数
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(OrderCancelRequest request) {
        // 1. 查询订单并校验所属用户
        MallOrder mallOrder = lambdaQuery()
                .eq(MallOrder::getOrderNo, request.getOrderNo())
                .one();

        if (mallOrder == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 2. 校验订单所属用户
        Long orderUserId = mallOrder.getUserId();
        Long userId = getUserId();
        if (!Objects.equals(orderUserId, userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在!");
        }

        // 3. 校验订单状态（只有待支付状态可以取消）
        if (!Objects.equals(mallOrder.getOrderStatus(), ORDER_STATUS_WAIT_PAY)) {
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
            String statusName = orderStatusEnum != null ? orderStatusEnum.getName() : "未知";
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许取消", statusName));
        }

        // 4. 更新订单状态为已取消
        Date now = new Date();
        mallOrder.setOrderStatus(OrderStatusEnum.CANCELLED.getType());
        mallOrder.setPayType(PayTypeEnum.CANCELLED.getType());
        mallOrder.setCloseReason(request.getCancelReason());
        mallOrder.setCloseTime(now);
        mallOrder.setUpdateTime(now);

        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "取消订单失败，请重试");
        }

        // 5. 恢复库存
        Long orderId = mallOrder.getId();
        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, orderId)
                .list();

        if (orderItems != null && !orderItems.isEmpty()) {
            for (MallOrderItem orderItem : orderItems) {
                if (orderItem != null && orderItem.getProductId() != null && orderItem.getQuantity() != null) {
                    mallProductService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
                }
            }
        }

        // 6. 添加订单时间线记录
        String username = getUsername();
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(orderId)
                .eventType(OrderEventTypeEnum.ORDER_CANCELLED.getType())
                .eventStatus(OrderStatusEnum.CANCELLED.getType())
                .operatorType(OperatorTypeEnum.USER.getType())
                .description(String.format("用户%s取消了订单，原因：%s", username, request.getCancelReason()))
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        log.info("用户{}取消订单成功，订单号：{}，原因：{}", username, mallOrder.getOrderNo(), request.getCancelReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceipt(OrderReceiveRequest request) {
        // 1. 查询订单并校验所属用户
        MallOrder mallOrder = lambdaQuery()
                .eq(MallOrder::getOrderNo, request.getOrderNo())
                .one();

        if (mallOrder == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 2. 校验订单所属用户
        Long orderUserId = mallOrder.getUserId();
        Long userId = getUserId();
        if (!Objects.equals(orderUserId, userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在!");
        }

        // 3. 校验订单状态是否允许确认收货
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        if (orderStatusEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }

        // 只有待收货状态可以确认收货
        if (orderStatusEnum != OrderStatusEnum.PENDING_RECEIPT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许确认收货", orderStatusEnum.getName()));
        }

        // 4. 更新订单状态为已完成
        Date now = new Date();
        mallOrder.setOrderStatus(OrderStatusEnum.COMPLETED.getType());
        mallOrder.setReceiveTime(now);
        mallOrder.setFinishTime(now);
        mallOrder.setUpdateTime(now);

        boolean updated = updateById(mallOrder);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "确认收货失败，请重试");
        }

        // 5. 更新物流状态为已签收
        Long orderId = mallOrder.getId();
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(orderId);
        if (shipping != null) {
            shipping.setStatus(ShippingStatusEnum.DELIVERED.getType());
            shipping.setReceiveTime(now);
            shipping.setUpdateTime(now);
            mallOrderShippingService.updateById(shipping);
        }

        // 6. 添加订单时间线记录（标记为用户操作）
        String username = getUsername();
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(orderId)
                .eventType(OrderEventTypeEnum.ORDER_RECEIVED.getType())
                .eventStatus(OrderStatusEnum.COMPLETED.getType())
                .operatorType(OperatorTypeEnum.USER.getType())
                .description(String.format("用户%s确认收货", username))
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(timelineDto);

        // 订单完成后按销量增量阈值刷新商品索引
        runAfterCommit(() -> {
            try {
                syncSalesIndexIfNeeded(orderId);
            } catch (Exception ex) {
                log.warn("订单{}销量索引同步失败: {}", orderId, ex.getMessage(), ex);
            }
        });

        log.info("用户{}确认收货成功，订单号：{}", username, mallOrder.getOrderNo());
        return true;
    }

    @Override
    public OrderShippingVo getOrderShipping(String orderNo) {
        // 1. 查询订单基本信息并校验所属用户
        MallOrder mallOrder = lambdaQuery()
                .eq(MallOrder::getOrderNo, orderNo)
                .one();

        if (mallOrder == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 2. 校验订单所属用户
        Long orderUserId = mallOrder.getUserId();
        Long userId = getUserId();
        if (!Objects.equals(orderUserId, userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在!");
        }

        // 3. 查询物流信息
        Long orderId = mallOrder.getId();
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(orderId);

        // 4. 获取订单状态名称
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(mallOrder.getOrderStatus());
        String orderStatusName = orderStatusEnum != null ? orderStatusEnum.getName() : "未知";

        // 5. 组装收货人信息
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(mallOrder.getDeliveryType());
        OrderShippingVo.ReceiverInfo receiverInfo = OrderShippingVo.ReceiverInfo.builder()
                .receiverName(mallOrder.getReceiverName())
                .receiverPhone(mallOrder.getReceiverPhone())
                .receiverDetail(mallOrder.getReceiverDetail())
                .deliveryType(mallOrder.getDeliveryType())
                .deliveryTypeName(deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知")
                .build();

        // 6. 组装返回VO
        OrderShippingVo.OrderShippingVoBuilder builder = OrderShippingVo.builder()
                .orderId(mallOrder.getId())
                .orderNo(mallOrder.getOrderNo())
                .orderStatus(mallOrder.getOrderStatus())
                .orderStatusName(orderStatusName)
                .receiverInfo(receiverInfo);

        // 7. 如果有物流信息，添加物流详情
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
    public Page<OrderListVo> getOrderList(OrderListRequest request) {
        Long userId = getUserId();
        Page<MallOrderDto> page = request.toPage();

        // 查询订单列表DTO
        Page<MallOrderDto> orderDtoPage = baseMapper.selectOrderList(page, request, userId);

        // 将DTO转换为VO
        List<OrderListVo> orderVoList = BeanCotyUtils.copyListProperties(orderDtoPage.getRecords(), OrderListVo.class);


        // 查询每个订单的商品项
        for (OrderListVo orderVo : orderVoList) {
            // 获取对应的DTO
            MallOrderDto orderDto = orderDtoPage.getRecords().stream()
                    .filter(dto -> dto.getId().equals(orderVo.getId()))
                    .findFirst()
                    .orElse(null);

            // 获取订单状态名称
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(orderVo.getOrderStatus());
            orderVo.setOrderStatusName(orderStatusEnum != null ? orderStatusEnum.getName() : "未知");

            // 设置收货人信息
            if (orderDto != null && (orderDto.getReceiverName() != null || orderDto.getReceiverPhone() != null || orderDto.getReceiverDetail() != null)) {
                OrderListVo.ReceiverInfo receiverInfo = OrderListVo.ReceiverInfo.builder()
                        .name(orderDto.getReceiverName())
                        .phone(orderDto.getReceiverPhone())
                        .address(orderDto.getReceiverDetail())
                        .build();
                orderVo.setReceiverInfo(receiverInfo);
            }

            // 查询订单项
            List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                    .eq(MallOrderItem::getOrderId, orderVo.getId())
                    .list();

            // 一个订单可能有多个商品项,需要遍历处理 需要转换成VO
            List<OrderListVo.OrderItemSimpleVo> itemVos = new ArrayList<>();
            for (MallOrderItem item : orderItems) {
                OrderItemAfterSaleStatusEnum afterSaleStatusEnum =
                        OrderItemAfterSaleStatusEnum.fromCode(item.getAfterSaleStatus());

                OrderListVo.OrderItemSimpleVo itemVo = OrderListVo.OrderItemSimpleVo.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalPrice(item.getTotalPrice())
                        .afterSaleStatus(item.getAfterSaleStatus())
                        .afterSaleStatusName(afterSaleStatusEnum != null ? afterSaleStatusEnum.getName() : "未知")
                        .build();
                itemVos.add(itemVo);
            }
            orderVo.setItems(itemVos);
        }

        // 构建返回的Page对象
        Page<OrderListVo> resultPage = new Page<>(orderDtoPage.getCurrent(), orderDtoPage.getSize(), orderDtoPage.getTotal());
        resultPage.setRecords(orderVoList);

        return resultPage;
    }

    @Override
    public OrderDetailVo getOrderDetail(String orderNo) {
        Long userId = getUserId();

        // 1. 查询订单详情
        OrderDetailDto orderDetailDto = baseMapper.getOrderDetailByOrderNo(orderNo, userId);
        if (orderDetailDto == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 2. 设置订单状态名称
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(orderDetailDto.getOrderStatus());

        // 3. 设置支付方式名称
        PayTypeEnum payTypeEnum = PayTypeEnum.fromCode(orderDetailDto.getPayType());

        // 4. 设置配送方式名称
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(orderDetailDto.getDeliveryType());

        // 5. 从 SQL 查询结果中获取收货人信息
        OrderDetailVo.ReceiverInfo receiverInfo = OrderDetailVo.ReceiverInfo.builder()
                .receiverName(orderDetailDto.getReceiverName())
                .receiverPhone(orderDetailDto.getReceiverPhone())
                .receiverDetail(orderDetailDto.getReceiverDetail())
                .build();

        // 6. 查询订单项
        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, orderDetailDto.getId())
                .list();

        List<OrderDetailVo.OrderItemDetailVo> itemDetailVos = new ArrayList<>();
        for (MallOrderItem item : orderItems) {
            OrderItemAfterSaleStatusEnum afterSaleStatusEnum =
                    OrderItemAfterSaleStatusEnum.fromCode(item.getAfterSaleStatus());

            OrderDetailVo.OrderItemDetailVo itemDetailVo = OrderDetailVo.OrderItemDetailVo.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .imageUrl(item.getImageUrl())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalPrice(item.getTotalPrice())
                    .afterSaleStatus(item.getAfterSaleStatus())
                    .afterSaleStatusName(afterSaleStatusEnum != null ? afterSaleStatusEnum.getName() : "未知")
                    .refundedAmount(item.getRefundedAmount())
                    .build();
            itemDetailVos.add(itemDetailVo);
        }

        // 7. 查询物流信息
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(orderDetailDto.getId());
        OrderDetailVo.ShippingInfo shippingInfo = null;
        if (shipping != null) {
            ShippingStatusEnum shippingStatusEnum = ShippingStatusEnum.fromCode(shipping.getStatus());
            shippingInfo = OrderDetailVo.ShippingInfo.builder()
                    .logisticsCompany(shipping.getShippingCompany())
                    .trackingNumber(shipping.getShippingNo())
                    .shippingStatus(shipping.getStatus())
                    .shippingStatusName(shippingStatusEnum != null ? shippingStatusEnum.getName() : "未知")
                    .shipTime(shipping.getDeliverTime())
                    .build();
        }

        // 8. 构建并返回 OrderDetailVo
        return OrderDetailVo.builder()
                .id(orderDetailDto.getId())
                .orderNo(orderDetailDto.getOrderNo())
                .orderStatus(orderDetailDto.getOrderStatus())
                .orderStatusName(orderStatusEnum != null ? orderStatusEnum.getName() : "未知")
                .totalAmount(orderDetailDto.getTotalAmount())
                .payAmount(orderDetailDto.getPayAmount())
                .freightAmount(orderDetailDto.getFreightAmount())
                .payType(orderDetailDto.getPayType())
                .payTypeName(payTypeEnum != null ? payTypeEnum.getType() : "未知")
                .deliveryType(orderDetailDto.getDeliveryType())
                .deliveryTypeName(deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知")
                .paid(orderDetailDto.getPaid())
                .payExpireTime(orderDetailDto.getPayExpireTime())
                .payTime(orderDetailDto.getPayTime())
                .deliverTime(orderDetailDto.getDeliverTime())
                .receiveTime(orderDetailDto.getReceiveTime())
                .finishTime(orderDetailDto.getFinishTime())
                .createTime(orderDetailDto.getCreateTime())
                .note(orderDetailDto.getNote())
                .afterSaleFlag(OrderItemAfterSaleStatusEnum.fromCode(orderDetailDto.getAfterSaleFlag()))
                .refundStatus(orderDetailDto.getRefundStatus())
                .refundPrice(orderDetailDto.getRefundPrice())
                .refundTime(orderDetailDto.getRefundTime())
                .receiverInfo(receiverInfo)
                .items(itemDetailVos)
                .shippingInfo(shippingInfo)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCheckoutVo createOrderFromCart(CartSettleRequest request) {
        Long userId = SecurityUtils.getUserId();

        // 1. 查询购物车商品
        List<MallCart> cartItems = mallCartService.lambdaQuery()
                .eq(MallCart::getUserId, userId)
                .in(MallCart::getId, request.getCartIds())
                .list();

        if (cartItems.isEmpty()) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "购物车商品不存在");
        }

        // 2. 校验商品并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<MallOrderItem> orderItems = new ArrayList<>();

        for (MallCart cartItem : cartItems) {
            // 查询商品详情
            MallProductWithImageDto product = mallProductService.getProductWithImagesById(cartItem.getProductId());
            if (product == null) {
                throw new ServiceException(ResponseCode.RESULT_IS_NULL,
                        String.format("商品[%s]不存在", cartItem.getProductName()));
            }

            // 校验商品状态
            if (product.getStatus() != 1) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        String.format("商品[%s]已下架", product.getName()));
            }

            // 校验库存
            if (product.getStock() < cartItem.getCartNum()) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        String.format("商品[%s]库存不足，当前库存：%d", product.getName(), product.getStock()));
            }

            // 扣减库存
            mallProductService.deductStock(product.getId(), cartItem.getCartNum());

            // 计算小计
            BigDecimal itemTotal = product.getPrice()
                    .multiply(new BigDecimal(cartItem.getCartNum()))
                    .setScale(2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(itemTotal);

            // 准备订单项数据
            String imageUrl = null;
            if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                imageUrl = product.getProductImages().getFirst().getImageUrl();
            }

            MallOrderItem orderItem = MallOrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(imageUrl)
                    .price(product.getPrice())
                    .quantity(cartItem.getCartNum())
                    .totalPrice(itemTotal)
                    .afterSaleStatus(OrderItemAfterSaleStatusEnum.NONE.getStatus())
                    .build();

            orderItems.add(orderItem);
        }

        // 3. 生成订单号
        String orderNo = generateOrderNo();
        Date now = new Date();
        UserAddress userAddress = getUserAddressOrThrow(userId, request.getAddressId());
        String receiverDetail = buildReceiverDetail(userAddress);

        // 4. 创建订单，使用用户选择的配送方式
        MallOrder order = MallOrder.builder()
                .orderNo(orderNo)
                .userId(userId)
                .totalAmount(totalAmount)
                .payAmount(BigDecimal.ZERO)
                .freightAmount(BigDecimal.ZERO)
                .orderStatus(ORDER_STATUS_WAIT_PAY)
                .payType(WAIT_PAY)
                .paid(0)
                .deliveryType(request.getDeliveryType().getType())
                .addressId(userAddress.getId())
                .receiverName(userAddress.getReceiverName())
                .receiverPhone(userAddress.getReceiverPhone())
                .receiverDetail(receiverDetail)
                .note(request.getRemark())
                .afterSaleFlag(OrderItemAfterSaleStatusEnum.NONE)
                .createTime(now)
                .updateTime(now)
                .build();

        boolean orderSaved = save(order);
        if (!orderSaved) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单创建失败");
        }

        // 5. 保存订单项
        for (MallOrderItem item : orderItems) {
            item.setOrderId(order.getId());
            item.setCreateTime(now);
            item.setUpdateTime(now);
        }
        boolean itemsSaved = mallOrderItemService.saveBatch(orderItems);
        if (!itemsSaved) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单项保存失败");
        }

        // 6. 添加订单时间线
        OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                .orderId(order.getId())
                .eventType(OrderEventTypeEnum.ORDER_CREATED.getType())
                .eventStatus(ORDER_STATUS_WAIT_PAY)
                .operatorType(OperatorTypeEnum.USER.getType())
                .description("用户创建订单")
                .build();
        mallOrderTimelineService.addTimeline(timelineDto);

        // 7. 发送延时消息（订单超时自动取消）
        orderDelayProducer.addOrderToDelayQueue(orderNo, ORDER_TIMEOUT_MINUTES);

        // 9. 删除已结算的购物车商品
        mallCartService.removeCartItems(request.getCartIds());

        // 10. 构建商品摘要
        String productSummary = orderItems.stream()
                .map(MallOrderItem::getProductName)
                .collect(Collectors.joining("、"));

        // 11. 计算过期时间
        Date expireTime = Date.from(LocalDateTime.now()
                .plusMinutes(ORDER_TIMEOUT_MINUTES)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        log.info("用户{}从购物车创建订单成功，订单号：{}，共{}件商品",
                userId, orderNo, orderItems.size());

        return OrderCheckoutVo.builder()
                .orderNo(orderNo)
                .totalAmount(totalAmount)
                .productSummary(productSummary)
                .itemCount(orderItems.size())
                .orderStatus(ORDER_STATUS_WAIT_PAY)
                .createTime(now)
                .payExpireTime(expireTime)
                .build();
    }

    @Override
    public OrderPreviewVo previewOrder(OrderPreviewRequest request) {
        Long userId = getUserId();
        UserAddress userAddress = getUserAddressOrThrow(userId, request.getAddressId());
        // 根据预览类型处理
        if (request.getType() == OrderPreviewRequest.PreviewType.PRODUCT) {
            // 单个商品购买预览
            return previewSingleProduct(request, userAddress);
        } else if (request.getType() == OrderPreviewRequest.PreviewType.CART) {
            // 购物车结算预览
            return previewCartItems(request, userId, userAddress);
        } else {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "不支持的预览类型");
        }
    }

    /**
     * 预览单个商品购买
     */
    private OrderPreviewVo previewSingleProduct(OrderPreviewRequest request, UserAddress userAddress) {
        if (request.getProductId() == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品ID不能为空");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "购买数量必须大于0");
        }

        // 查询商品详情
        MallProductWithImageDto product = mallProductService.getProductWithImagesById(request.getProductId());
        if (product == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }

        // 校验商品状态
        final Integer PRODUCT_STATUS_ON_SALE = 1;
        if (!Objects.equals(product.getStatus(), PRODUCT_STATUS_ON_SALE)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品未上架或已下架");
        }

        // 构建商品项预览
        String imageUrl = null;
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            imageUrl = product.getProductImages().getFirst().getImageUrl();
        }

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        OrderPreviewVo.OrderItemPreview itemPreview = OrderPreviewVo.OrderItemPreview.builder()
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(imageUrl)
                .price(product.getPrice())
                .quantity(request.getQuantity())
                .subtotal(subtotal)
                .stock(product.getStock())
                .status(product.getStatus())
                .statusDesc(product.getStatus() == 1 ? "在售" : "已下架")
                .build();

        // 计算价格
        DeliveryTypeEnum tempDeliveryTypeEnum = DeliveryTypeEnum.fromLegacyCode(product.getDeliveryType());
        String tempDeliveryType = tempDeliveryTypeEnum != null ? tempDeliveryTypeEnum.getType() : null;
        BigDecimal freightAmount = calculateFreight(tempDeliveryType, subtotal);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(freightAmount).subtract(discountAmount);

        // 获取配送方式信息
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromLegacyCode(product.getDeliveryType());
        String deliveryType = deliveryTypeEnum != null ? deliveryTypeEnum.getType() : "UNKNOWN";
        String deliveryTypeName = deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知";

        return OrderPreviewVo.builder()
                .items(List.of(itemPreview))
                .itemsAmount(subtotal)
                .freightAmount(freightAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .address(buildReceiverDetail(userAddress))
                .deliveryType(deliveryType)
                .deliveryTypeName(deliveryTypeName)
                .estimatedDeliveryTime(getEstimatedDeliveryTime(deliveryType))
                .build();
    }

    /**
     * 预览购物车商品
     */
    private OrderPreviewVo previewCartItems(OrderPreviewRequest request, Long userId, UserAddress userAddress) {
        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "购物车商品ID列表不能为空");
        }

        // 查询购物车商品
        List<MallCart> cartItems = mallCartService.lambdaQuery()
                .eq(MallCart::getUserId, userId)
                .in(MallCart::getId, request.getCartIds())
                .list();

        if (cartItems.isEmpty()) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "购物车商品不存在");
        }

        // 构建商品项预览列表
        List<OrderPreviewVo.OrderItemPreview> itemPreviews = new ArrayList<>();
        BigDecimal itemsAmount = BigDecimal.ZERO;
        String orderDeliveryType = null;

        for (MallCart cartItem : cartItems) {
            // 查询商品详情
            MallProductWithImageDto product = mallProductService.getProductWithImagesById(cartItem.getProductId());
            if (product == null) {
                throw new ServiceException(ResponseCode.RESULT_IS_NULL,
                        String.format("商品[%s]不存在", cartItem.getProductName()));
            }

            // 校验商品状态
            if (product.getStatus() != 1) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR,
                        String.format("商品[%s]已下架", product.getName()));
            }

            // 校验并统一配送方式
            orderDeliveryType = getString(orderDeliveryType, product);

            // 构建商品项预览
            String imageUrl = null;
            if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                imageUrl = product.getProductImages().getFirst().getImageUrl();
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getCartNum()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderPreviewVo.OrderItemPreview itemPreview = OrderPreviewVo.OrderItemPreview.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(imageUrl)
                    .price(product.getPrice())
                    .quantity(cartItem.getCartNum())
                    .subtotal(subtotal)
                    .stock(product.getStock())
                    .status(product.getStatus())
                    .statusDesc(product.getStatus() == 1 ? "在售" : "已下架")
                    .build();

            itemPreviews.add(itemPreview);
            itemsAmount = itemsAmount.add(subtotal);
        }

        // 计算价格
        BigDecimal freightAmount = calculateFreight(orderDeliveryType, itemsAmount);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = itemsAmount.add(freightAmount).subtract(discountAmount);

        // 获取配送方式信息
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(orderDeliveryType);
        String deliveryTypeName = deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知";

        return OrderPreviewVo.builder()
                .items(itemPreviews)
                .itemsAmount(itemsAmount)
                .freightAmount(freightAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .address(buildReceiverDetail(userAddress))
                .deliveryType(orderDeliveryType)
                .deliveryTypeName(deliveryTypeName)
                .estimatedDeliveryTime(getEstimatedDeliveryTime(orderDeliveryType))
                .build();
    }

    /**
     * 根据ID获取并校验用户收货地址
     *
     * @param userId    当前用户ID
     * @param addressId 地址ID
     * @return 收货地址
     */
    private UserAddress getUserAddressOrThrow(Long userId, Long addressId) {
        if (addressId == null) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "请选择收货地址");
        }
        UserAddress address = userAddressService.getById(addressId);
        if (address == null || !Objects.equals(address.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "收货地址不存在");
        }
        return address;
    }

    /**
     * 构建完整收货地址
     *
     * @param address 收货地址
     * @return 完整地址
     */
    private String buildReceiverDetail(UserAddress address) {
        if (address == null) {
            return null;
        }
        StringBuilder detailBuilder = new StringBuilder();
        if (StringUtils.hasText(address.getAddress())) {
            detailBuilder.append(address.getAddress());
        }
        if (StringUtils.hasText(address.getDetailAddress())) {
            if (!detailBuilder.isEmpty()) {
                detailBuilder.append(" ");
            }
            detailBuilder.append(address.getDetailAddress());
        }
        return detailBuilder.toString();
    }

    /**
     * 获取配送方式信息
     */
    private String getString(String orderDeliveryType, MallProductWithImageDto product) {
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromLegacyCode(product.getDeliveryType());
        if (deliveryTypeEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("商品[%s]配送方式配置异常", product.getName()));
        }

        String productDeliveryType = deliveryTypeEnum.getType();
        if (orderDeliveryType == null) {
            orderDeliveryType = productDeliveryType;
        } else if (!orderDeliveryType.equals(productDeliveryType)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    "购物车中的商品配送方式不一致，请分开下单");
        }
        return orderDeliveryType;
    }

    /**
     * 计算运费
     */
    private BigDecimal calculateFreight(String deliveryType, BigDecimal itemsAmount) {
        // 这里可以根据配送方式和商品金额计算运费
        // 示例：满100免运费，否则收取10元运费
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(deliveryType);
        if (deliveryTypeEnum == null) {
            return BigDecimal.ZERO;
        }

        return switch (deliveryTypeEnum) {
            case EXPRESS ->
                // 快递配送：满100免运费
                    itemsAmount.compareTo(new BigDecimal("100")) >= 0
                            ? BigDecimal.ZERO
                            : new BigDecimal("10.00");
            case SELF_PICKUP ->
                // 自提：免运费
                    BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 获取预计送达时间
     */
    private String getEstimatedDeliveryTime(String deliveryType) {
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(deliveryType);
        if (deliveryTypeEnum == null) {
            return "未知";
        }

        return switch (deliveryTypeEnum) {
            case EXPRESS -> "预计3-5天送达";
            case SELF_PICKUP -> "下单后可到店自提";
            default -> "未知";
        };
    }

    private void syncSalesIndexIfNeeded(Long orderId) {
        if (orderId == null) {
            return;
        }
        List<MallOrderItem> orderItems = mallOrderItemService.getOrderItemByOrderId(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        Map<Long, Integer> incrementMap = orderItems.stream()
                .filter(item -> item.getProductId() != null && item.getQuantity() != null)
                .collect(Collectors.toMap(
                        MallOrderItem::getProductId,
                        MallOrderItem::getQuantity,
                        Integer::sum
                ));
        if (incrementMap.isEmpty()) {
            return;
        }

        List<Long> productIdsToSync = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : incrementMap.entrySet()) {
            Integer increment = entry.getValue();
            if (increment == null || increment <= 0) {
                continue;
            }
            String counterKey = String.format(RedisConstants.MallProductIndex.SALES_SYNC_COUNTER_KEY, entry.getKey());
            Long counter = redisCache.redisTemplate.opsForValue().increment(counterKey, increment);
            long current = counter != null ? counter : increment;
            long previous = current - increment;
            if (previous / SALES_SYNC_THRESHOLD < current / SALES_SYNC_THRESHOLD) {
                productIdsToSync.add(entry.getKey());
            }
        }

        if (productIdsToSync.isEmpty()) {
            return;
        }

        Map<Long, Integer> salesMap = mallOrderItemService.getCompletedSalesByProductIds(productIdsToSync);
        List<MallProductDocument> documents = new ArrayList<>();
        for (Long productId : productIdsToSync) {
            try {
                MallProductDetailDto detail = mallProductService.getProductAndDrugInfoById(productId);
                if (detail == null) {
                    continue;
                }
                detail.setSales(salesMap.getOrDefault(productId, 0));
                MallProductDocument document = toSearchDocument(detail);
                if (document != null) {
                    documents.add(document);
                }
            } catch (Exception ex) {
                log.warn("商品{}销量索引同步失败: {}", productId, ex.getMessage(), ex);
            }
        }
        if (!documents.isEmpty()) {
            mallProductSearchService.saveAll(documents);
        }
    }

    private MallProductDocument toSearchDocument(MallProductDetailDto detail) {
        if (detail == null) {
            return null;
        }
        DrugDetailDto drugDetail = detail.getDrugDetail();
        String coverImage = detail.getImages() != null && !detail.getImages().isEmpty() ? detail.getImages().getFirst() : null;
        return MallProductDocument.builder()
                .id(detail.getId())
                .name(detail.getName())
                .categoryName(detail.getCategoryName())
                .price(detail.getPrice())
                .sales(detail.getSales())
                .prescription(drugDetail != null ? drugDetail.getPrescription() : null)
                .status(detail.getStatus())
                .brand(drugDetail != null ? drugDetail.getBrand() : null)
                .commonName(drugDetail != null ? drugDetail.getCommonName() : null)
                .efficacy(drugDetail != null ? drugDetail.getEfficacy() : null)
                .instruction(drugDetail != null ? drugDetail.getInstruction() : null)
                .coverImage(coverImage)
                .nameSuggest(completion(detail.getName()))
                .commonNameSuggest(completion(drugDetail != null ? drugDetail.getCommonName() : null))
                .brandSuggest(completion(drugDetail != null ? drugDetail.getBrand() : null))
                .build();
    }

    private Completion completion(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return new Completion(List.of(value));
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
