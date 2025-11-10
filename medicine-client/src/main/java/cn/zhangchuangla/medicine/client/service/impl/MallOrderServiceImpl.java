package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.client.model.request.OrderConfirmRequest;
import cn.zhangchuangla.medicine.client.model.request.OrderCreateRequest;
import cn.zhangchuangla.medicine.client.model.request.OrderListRequest;
import cn.zhangchuangla.medicine.client.model.request.OrderReceiveRequest;
import cn.zhangchuangla.medicine.client.model.vo.OrderCreateVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.client.model.vo.OrderListVo;
import cn.zhangchuangla.medicine.client.service.*;
import cn.zhangchuangla.medicine.client.task.OrderDelayProducer;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.AlipayNotifyDTO;
import cn.zhangchuangla.medicine.model.dto.OrderTimelineDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.zhangchuangla.medicine.common.core.constants.Constants.ORDER_TIMEOUT_MINUTES;

/**
 * @author Chuang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService, BaseService {

    private static final String ORDER_STATUS_WAIT_PAY = OrderStatusEnum.PENDING_PAYMENT.getType();
    private static final String ORDER_STATUS_WAIT_SHIPMENT = OrderStatusEnum.PENDING_SHIPMENT.getType();
    private static final String PAY_TYPE_ALIPAY = PayTypeEnum.ALIPAY.getType();
    private static final String WAIT_PAY = PayTypeEnum.WAIT_PAY.getType();

    private final MallProductService mallProductService;
    private final MallOrderItemService mallOrderItemService;
    private final AlipayPaymentService alipayPaymentService;
    private final AlipayProperties alipayProperties;
    private final OrderDelayProducer orderDelayProducer;
    private final UserWalletService userWalletService;
    private final MallOrderTimelineService mallOrderTimelineService;
    private final MallOrderShippingService mallOrderShippingService;


    /**
     * 创建商城订单的核心流程：校验库存 → 扣减库存 → 构建订单 → 返回支付信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVo createOrder(OrderCreateRequest request) {
        // 1. 查询商品详情并校验上架状态
        MallProductWithImageDto mallProductWithImageDto = mallProductService.getProductWithImagesById(request.getProductId());
        if (mallProductWithImageDto == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }
        BigDecimal totalAmount = validateProductAndCalculateAmount(request, mallProductWithImageDto);

        // 4. 扣减库存，内部包含乐观锁控制
        mallProductService.deductStock(request.getProductId(), request.getQuantity());

        // 5. 生成业务订单号并补充订单基础信息
        String orderNo = generateOrderNo();
        Date now = new Date();

        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromLegacyCode(mallProductWithImageDto.getDeliveryType());
        if (deliveryTypeEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品配送方式配置异常");
        }
        String deliveryTypeCode = deliveryTypeEnum.getType();

        MallOrder order = MallOrder.builder()
                .orderNo(orderNo)
                .userId(SecurityUtils.getUserId())
                .totalAmount(totalAmount)
                .payAmount(BigDecimal.ZERO)
                .freightAmount(BigDecimal.ZERO)
                .payType(WAIT_PAY)
                .orderStatus(ORDER_STATUS_WAIT_PAY)
                .deliveryType(deliveryTypeCode)
                .receiverDetail(request.getAddress())
                .note(request.getRemark())
                .afterSaleFlag(OrderItemAfterSaleStatusEnum.NONE)
                .createTime(now)
                .updateTime(now)
                .build();

        // 6. 先保存订单
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

        // 7. 保存订单项
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

        return OrderCreateVo.builder()
                .orderNo(orderNo)
                .totalAmount(totalAmount)
                .status(order.getOrderStatus())
                .createTime(now)
                .expireTime(expireTime)
                .productSummary(buildProductSummary(mallProductWithImageDto, request.getQuantity()))
                .build();
    }

    /**
     * 校验商品状态与库存，并计算订单总金额。
     */
    private BigDecimal validateProductAndCalculateAmount(OrderCreateRequest request, MallProduct product) {
        final Integer PRODUCT_STATUS_ON_SALE = 1;
        if (product == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }
        if (!Objects.equals(product.getStatus(), PRODUCT_STATUS_ON_SALE)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品未上架或已下架");
        }
        // 2. 校验库存是否满足下单数量
        Integer stock = product.getStock();
        if (stock == null || stock < request.getQuantity()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("商品库存不足，当前库存：%d", stock == null ? 0 : stock));
        }

        // 3. 计算订单应付金额（示例中不包含运费、优惠）
        BigDecimal price = product.getPrice();
        if (price == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "商品价格未配置");
        }
        return price.multiply(BigDecimal.valueOf(request.getQuantity()));
    }

    /**
     * 查询订单的支付关键信息，确保在支付前再做一次状态校验。
     */
    @Override
    public OrderCreateVo getOrderPayInfo(String orderNo) {
        // 使用订单号查询待支付订单，避免重复支付
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, orderNo)
                .one();
        checkOrderStatus(order);
        checkOrderOwnerUser(order);
        // 以 VO 格式返回支付关键信息，供前端拼装支付请求或确认页面
        return OrderCreateVo.builder()
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getOrderStatus())
                .createTime(order.getCreateTime())
                .productSummary("商城订单-" + order.getOrderNo())
                .build();
    }

    /**
     * 校验订单所属用户
     */
    private void checkOrderOwnerUser(MallOrder order) {
        Long orderUserId = order.getUserId();
        Long userId = getUserId();
        if (!Objects.equals(orderUserId, userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在!");
        }
    }


    /**
     * 确认订单
     *
     * @param request 确认订单请求参数
     * @return 订单支付表单信息
     */
    @Override
    public String confirmOrder(OrderConfirmRequest request) {
        // 使用订单号查询待支付订单，避免重复支付
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, request.getOrderNo())
                .one();
        checkOrderStatus(order);
        checkOrderOwnerUser(order);

        // 根据支付方式切换支付方式
        return switch (request.getPayMethod()) {
            case ALIPAY -> alipayPay(order);
            case WALLET -> walletPay(order);
            default -> throw new ServiceException(ResponseCode.OPERATION_ERROR, "不支持的支付方式");
        };
    }

    /**
     * 校验订单状态
     */
    private void checkOrderStatus(MallOrder order) {
        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }
        if (!Objects.equals(order.getOrderStatus(), ORDER_STATUS_WAIT_PAY)) {
            OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(order.getOrderStatus());
            String description = statusEnum != null ? statusEnum.getName() : "未知状态";
            String hint = String.format("订单状态异常，请勿重复支付，当前状态：%s", description);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, hint);
        }
    }

    /**
     * 支付宝支付
     */
    private String alipayPay(MallOrder order) {
        BigDecimal amount = order.getTotalAmount();
        if (amount == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单金额缺失，无法发起支付");
        }

        MallOrderItem firstItem = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, order.getId())
                .orderByAsc(MallOrderItem::getId)
                .last("LIMIT 1")
                .one();

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

        String totalAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();

        String notifyUrl = alipayProperties.getNotifyUrl();
        String returnUrl = alipayProperties.getReturnUrl();
        log.info("构建支付宝页面支付表单，orderNo={}，notifyUrl={}，returnUrl={}", order.getOrderNo(), notifyUrl, returnUrl);

        AlipayPagePayRequest payRequest = AlipayPagePayRequest.builder()
                .outTradeNo(order.getOrderNo())
                .subject(subject)
                .totalAmount(totalAmount)
                .body("商城订单支付（订单号：" + order.getOrderNo() + "）")
                .timeoutExpress(ORDER_TIMEOUT_MINUTES + "m")
                .notifyUrl(notifyUrl)
                .returnUrl(returnUrl)
                .build();

        return alipayPaymentService.generatePagePayForm(payRequest);
    }


    /**
     * 钱包支付
     */
    private String walletPay(MallOrder order) {
        BigDecimal totalAmount = order.getTotalAmount();
        Long userId = getUserId();
        boolean result = userWalletService.deductBalance(userId, totalAmount, String.format("订单支付-%s", order.getOrderNo()));
        if (result) {
            markOrderPaid(order.getOrderNo(), totalAmount, PayTypeEnum.WALLET.getType());
        }
        return "支付成功";
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
            String username = getUsername();
            PayTypeEnum payTypeEnum = PayTypeEnum.fromCode(payType);
            String payTypeName = payTypeEnum != null ? payTypeEnum.getDescription() : "未知支付方式";
            OrderTimelineDto timelineDto = OrderTimelineDto.builder()
                    .orderId(order.getId())
                    .eventType(OrderEventTypeEnum.ORDER_PAID.getType())
                    .eventStatus(ORDER_STATUS_WAIT_SHIPMENT)
                    .operatorType(OperatorTypeEnum.USER.getType())
                    .description(String.format("用户%s使用%s完成了订单支付", username, payTypeName))
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
        Page<OrderListVo> page = request.toPage();

        // 查询订单列表
        Page<OrderListVo> orderPage = baseMapper.selectOrderList(page, request, userId);

        // 查询每个订单的商品项
        for (OrderListVo orderVo : orderPage.getRecords()) {
            // 获取订单状态名称
            OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(orderVo.getOrderStatus());
            orderVo.setOrderStatusName(orderStatusEnum != null ? orderStatusEnum.getName() : "未知");

            // 查询订单项
            List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                    .eq(MallOrderItem::getOrderId, orderVo.getId())
                    .list();

            // 转换为简化VO
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

        return orderPage;
    }

    @Override
    public OrderDetailVo getOrderDetail(String orderNo) {
        Long userId = getUserId();

        // 1. 查询订单详情
        OrderDetailVo orderDetailVo = baseMapper.selectOrderDetail(orderNo, userId);
        if (orderDetailVo == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 2. 设置订单状态名称
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(orderDetailVo.getOrderStatus());
        orderDetailVo.setOrderStatusName(orderStatusEnum != null ? orderStatusEnum.getName() : "未知");

        // 3. 设置支付方式名称
        PayTypeEnum payTypeEnum = PayTypeEnum.fromCode(orderDetailVo.getPayType());
        orderDetailVo.setPayTypeName(payTypeEnum != null ? payTypeEnum.getType() : "未知");

        // 4. 设置配送方式名称
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(orderDetailVo.getDeliveryType());
        orderDetailVo.setDeliveryTypeName(deliveryTypeEnum != null ? deliveryTypeEnum.getName() : "未知");

        // 5. 从 SQL 查询结果中获取收货人信息并重新构建
        OrderDetailVo.ReceiverInfo receiverInfo = OrderDetailVo.ReceiverInfo.builder()
                .receiverName(orderDetailVo.getReceiverName())
                .receiverPhone(orderDetailVo.getReceiverPhone())
                .receiverDetail(orderDetailVo.getReceiverDetail())
                .build();
        orderDetailVo.setReceiverInfo(receiverInfo);

        // 6. 查询订单项
        Long orderId = orderDetailVo.getId();
        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, orderId)
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
        orderDetailVo.setItems(itemDetailVos);

        // 7. 查询物流信息
        MallOrderShipping shipping = mallOrderShippingService.getByOrderId(orderId);
        if (shipping != null) {
            ShippingStatusEnum shippingStatusEnum = ShippingStatusEnum.fromCode(shipping.getStatus());

            OrderDetailVo.ShippingInfo shippingInfo = OrderDetailVo.ShippingInfo.builder()
                    .logisticsCompany(shipping.getShippingCompany())
                    .trackingNumber(shipping.getShippingNo())
                    .shippingStatus(shipping.getStatus())
                    .shippingStatusName(shippingStatusEnum != null ? shippingStatusEnum.getName() : "未知")
                    .shipTime(shipping.getDeliverTime())
                    .build();
            orderDetailVo.setShippingInfo(shippingInfo);
        }

        return orderDetailVo;
    }
}
