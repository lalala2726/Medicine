package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallAfterSaleMapper;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleAuditRequest;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleListRequest;
import cn.zhangchuangla.medicine.admin.model.request.AfterSaleProcessRequest;
import cn.zhangchuangla.medicine.admin.service.*;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.dto.OrderTimelineDto;
import cn.zhangchuangla.medicine.model.entity.MallAfterSale;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleListVo;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleTimelineVo;
import cn.zhangchuangla.medicine.payment.model.AlipayRefundRequest;
import cn.zhangchuangla.medicine.payment.service.AlipayPaymentService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 售后申请Service实现(管理端)
 *
 * @author Chuang
 * created 2025/11/08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallAfterSaleServiceImpl extends ServiceImpl<MallAfterSaleMapper, MallAfterSale>
        implements MallAfterSaleService {

    private final MallAfterSaleMapper mallAfterSaleMapper;
    private final MallAfterSaleTimelineService mallAfterSaleTimelineService;
    private final MallOrderItemService mallOrderItemService;
    private final MallOrderTimelineService mallOrderTimelineService;
    private final MallOrderService mallOrderService;
    private final UserService userService;
    private final UserWalletService userWalletService;
    private final AlipayPaymentService alipayPaymentService;

    @Override
    public Page<AfterSaleListVo> getAfterSaleList(AfterSaleListRequest request) {
        Page<AfterSaleListVo> page = request.toPage();
        return mallAfterSaleMapper.selectAfterSaleList(page, request);
    }

    @Override
    public AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId) {
        // 1. 查询售后申请
        MallAfterSale afterSale = getById(afterSaleId);
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        // 2. 查询用户信息
        User user = userService.getById(afterSale.getUserId());

        // 3. 查询订单项信息
        MallOrderItem orderItem = mallOrderItemService.getById(afterSale.getOrderItemId());

        // 4. 构建售后详情
        AfterSaleTypeEnum afterSaleTypeEnum = afterSale.getAfterSaleType();
        AfterSaleStatusEnum afterSaleStatusEnum = afterSale.getAfterSaleStatus();
        AfterSaleReasonEnum afterSaleReasonEnum = afterSale.getApplyReason();
        ReceiveStatusEnum receiveStatusEnum = afterSale.getReceiveStatus();

        List<String> evidenceImages = null;
        if (afterSale.getEvidenceImages() != null && !afterSale.getEvidenceImages().isEmpty()) {
            evidenceImages = JSON.parseArray(afterSale.getEvidenceImages(), String.class);
        }

        AfterSaleDetailVo.ProductInfo productInfo = null;
        if (orderItem != null) {
            productInfo = AfterSaleDetailVo.ProductInfo.builder()
                    .productId(orderItem.getProductId())
                    .productName(orderItem.getProductName())
                    .productImage(orderItem.getImageUrl())
                    .productPrice(orderItem.getPrice())
                    .quantity(orderItem.getQuantity())
                    .totalPrice(orderItem.getTotalPrice())
                    .build();
        }

        // 5. 查询时间线
        List<AfterSaleTimelineVo> timeline =
                mallAfterSaleTimelineService.getTimelineList(afterSaleId);

        return AfterSaleDetailVo.builder()
                .id(afterSale.getId())
                .afterSaleNo(afterSale.getAfterSaleNo())
                .orderId(afterSale.getOrderId())
                .orderNo(afterSale.getOrderNo())
                .orderItemId(afterSale.getOrderItemId())
                .userId(afterSale.getUserId())
                .userNickname(user != null ? user.getNickname() : "未知")
                .afterSaleType(afterSaleTypeEnum != null ? afterSaleTypeEnum.getType() : null)
                .afterSaleTypeName(afterSaleTypeEnum != null ? afterSaleTypeEnum.getName() : "未知")
                .afterSaleStatus(afterSaleStatusEnum != null ? afterSaleStatusEnum.getStatus() : null)
                .afterSaleStatusName(afterSaleStatusEnum != null ? afterSaleStatusEnum.getName() : "未知")
                .refundAmount(afterSale.getRefundAmount())
                .applyReason(afterSaleReasonEnum != null ? afterSaleReasonEnum.getReason() : null)
                .applyReasonName(afterSaleReasonEnum != null ? afterSaleReasonEnum.getName() : "未知")
                .applyDescription(afterSale.getApplyDescription())
                .evidenceImages(evidenceImages)
                .receiveStatus(receiveStatusEnum != null ? receiveStatusEnum.getStatus() : null)
                .receiveStatusName(receiveStatusEnum != null ? receiveStatusEnum.getName() : "未知")
                .rejectReason(afterSale.getRejectReason())
                .adminRemark(afterSale.getAdminRemark())
                .applyTime(afterSale.getApplyTime())
                .auditTime(afterSale.getAuditTime())
                .completeTime(afterSale.getCompleteTime())
                .productInfo(productInfo)
                .timeline(timeline)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditAfterSale(AfterSaleAuditRequest request) {
        Long adminId = SecurityUtils.getUserId();
        String adminUsername = SecurityUtils.getUsername();

        // 1. 查询售后申请
        MallAfterSale afterSale = getById(request.getAfterSaleId());
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        // 2. 校验售后状态
        AfterSaleStatusEnum afterSaleStatus = afterSale.getAfterSaleStatus();
        if (afterSaleStatus != AfterSaleStatusEnum.PENDING) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前售后状态[%s]不允许审核", afterSaleStatus != null ? afterSaleStatus.getName() : "未知"));
        }

        Date now = new Date();

        if (request.getApproved()) {
            // 审核通过
            afterSale.setAfterSaleStatus(AfterSaleStatusEnum.APPROVED);
            afterSale.setAdminRemark(request.getAdminRemark());
            afterSale.setAuditTime(now);
            afterSale.setUpdateTime(now);
            afterSale.setUpdateBy(adminUsername);

            boolean updated = updateById(afterSale);
            if (!updated) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "审核失败，请重试");
            }

            // 添加售后时间线记录
            String description = String.format("管理员%s审核通过了售后申请", adminUsername);
            mallAfterSaleTimelineService.addTimeline(
                    afterSale.getId(),
                    OrderEventTypeEnum.AFTER_SALE_APPROVED.getType(),
                    AfterSaleStatusEnum.APPROVED.getStatus(),
                    OperatorTypeEnum.ADMIN.getType(),
                    adminId,
                    description
            );

            // 添加订单时间线记录
            OrderTimelineDto orderTimelineDto = OrderTimelineDto.builder()
                    .orderId(afterSale.getOrderId())
                    .eventType(OrderEventTypeEnum.AFTER_SALE_APPROVED.getType())
                    .eventStatus(mallOrderService.getById(afterSale.getOrderId()).getOrderStatus())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .description(description)
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(orderTimelineDto);

            log.info("管理员{}审核通过售后申请，售后单号：{}", adminUsername, afterSale.getAfterSaleNo());

        } else {
            // 审核拒绝
            if (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty()) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "拒绝原因不能为空");
            }

            afterSale.setAfterSaleStatus(AfterSaleStatusEnum.REJECTED);
            afterSale.setRejectReason(request.getRejectReason());
            afterSale.setAdminRemark(request.getAdminRemark());
            afterSale.setAuditTime(now);
            afterSale.setCompleteTime(now);
            afterSale.setUpdateTime(now);
            afterSale.setUpdateBy(adminUsername);

            boolean updated = updateById(afterSale);
            if (!updated) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "审核失败，请重试");
            }

            // 更新订单项售后状态为无售后
            MallOrderItem orderItem = mallOrderItemService.getById(afterSale.getOrderItemId());
            if (orderItem != null) {
                orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.NONE.getStatus());
                orderItem.setUpdateTime(now);
                mallOrderItemService.updateById(orderItem);
            }

            // 检查订单是否还有其他售后
            long afterSaleCount = lambdaQuery()
                    .eq(MallAfterSale::getOrderId, afterSale.getOrderId())
                    .in(MallAfterSale::getAfterSaleStatus,
                            AfterSaleStatusEnum.PENDING.getStatus(),
                            AfterSaleStatusEnum.APPROVED.getStatus(),
                            AfterSaleStatusEnum.PROCESSING.getStatus())
                    .count();

            if (afterSaleCount == 0) {
                MallOrder order = mallOrderService.getById(afterSale.getOrderId());
                if (order != null) {
                    order.setAfterSaleFlag(OrderItemAfterSaleStatusEnum.NONE);
                    order.setUpdateTime(now);
                    mallOrderService.updateById(order);
                }
            }

            // 添加售后时间线记录
            String description = String.format("管理员%s拒绝了售后申请，原因：%s", adminUsername, request.getRejectReason());
            mallAfterSaleTimelineService.addTimeline(
                    afterSale.getId(),
                    OrderEventTypeEnum.AFTER_SALE_REJECTED.getType(),
                    AfterSaleStatusEnum.REJECTED.getStatus(),
                    OperatorTypeEnum.ADMIN.getType(),
                    adminId,
                    description
            );

            // 添加订单时间线记录
            OrderTimelineDto orderTimelineDto = OrderTimelineDto.builder()
                    .orderId(afterSale.getOrderId())
                    .eventType(OrderEventTypeEnum.AFTER_SALE_REJECTED.getType())
                    .eventStatus(mallOrderService.getById(afterSale.getOrderId()).getOrderStatus())
                    .operatorType(OperatorTypeEnum.ADMIN.getType())
                    .description(description)
                    .build();
            mallOrderTimelineService.addTimelineIfNotExists(orderTimelineDto);

            log.info("管理员{}拒绝售后申请，售后单号：{}，原因：{}", adminUsername, afterSale.getAfterSaleNo(), request.getRejectReason());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefund(AfterSaleProcessRequest request) {
        Long adminId = SecurityUtils.getUserId();
        String adminUsername = SecurityUtils.getUsername();

        // 1. 查询售后申请
        MallAfterSale afterSale = getById(request.getAfterSaleId());
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        // 2. 校验售后状态
        AfterSaleStatusEnum afterSaleStatus = afterSale.getAfterSaleStatus();
        if (afterSaleStatus != AfterSaleStatusEnum.APPROVED) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前售后状态[%s]不允许处理退款", afterSaleStatus != null ? afterSaleStatus.getName() : "未知"));
        }

        // 3. 校验售后类型
        AfterSaleTypeEnum afterSaleType = afterSale.getAfterSaleType();
        if (afterSaleType != AfterSaleTypeEnum.REFUND_ONLY && afterSaleType != AfterSaleTypeEnum.RETURN_REFUND) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "该售后类型不支持退款处理");
        }

        // 4. 查询订单信息
        MallOrder order = mallOrderService.getById(afterSale.getOrderId());
        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 校验订单是否已支付
        if (!Objects.equals(order.getPaid(), 1)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单未支付，无法退款");
        }

        // 5. 更新售后状态为处理中
        Date now = new Date();
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.PROCESSING);
        afterSale.setUpdateTime(now);
        afterSale.setUpdateBy(adminUsername);
        updateById(afterSale);

        // 6. 根据订单支付方式原路退款
        BigDecimal refundAmount = afterSale.getRefundAmount();
        PayTypeEnum payType = PayTypeEnum.fromCode(order.getPayType());

        try {
            switch (payType) {
                case ALIPAY:
                    processAlipayRefund(order, afterSale, refundAmount);
                    break;
                case WALLET:
                    processWalletRefund(afterSale, refundAmount);
                    break;
                default:
                    throw new ServiceException(ResponseCode.OPERATION_ERROR, "不支持的支付方式");
            }
        } catch (Exception e) {
            // 退款失败，恢复售后状态
            afterSale.setAfterSaleStatus(AfterSaleStatusEnum.APPROVED);
            updateById(afterSale);
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "退款失败：" + e.getMessage());
        }

        // 7. 更新订单退款信息
        BigDecimal orderRefundPrice = order.getRefundPrice() != null ? order.getRefundPrice() : BigDecimal.ZERO;
        BigDecimal totalRefunded = orderRefundPrice.add(refundAmount);
        order.setRefundPrice(totalRefunded);
        order.setRefundTime(now);
        order.setUpdateTime(now);

        // 判断是否全额退款
        BigDecimal payAmount = order.getPayAmount();
        if (payAmount != null && totalRefunded.compareTo(payAmount) >= 0) {
            order.setRefundStatus("SUCCESS");
            order.setOrderStatus(OrderStatusEnum.REFUNDED.getType());
        } else {
            order.setRefundStatus("PARTIAL");
            order.setOrderStatus(OrderStatusEnum.AFTER_SALE.getType());
        }
        mallOrderService.updateById(order);

        // 8. 更新订单项已退款金额
        MallOrderItem orderItem = mallOrderItemService.getById(afterSale.getOrderItemId());
        if (orderItem != null) {
            BigDecimal itemRefundedAmount = orderItem.getRefundedAmount() != null ? orderItem.getRefundedAmount() : BigDecimal.ZERO;
            orderItem.setRefundedAmount(itemRefundedAmount.add(refundAmount));
            orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.COMPLETED.getStatus());
            orderItem.setUpdateTime(now);
            mallOrderItemService.updateById(orderItem);
        }

        // 9. 更新售后状态为已完成
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.COMPLETED);
        afterSale.setCompleteTime(now);
        afterSale.setUpdateTime(now);
        afterSale.setUpdateBy(adminUsername);
        updateById(afterSale);

        // 10. 添加售后时间线记录
        String description = String.format("管理员%s完成了退款处理，退款金额：%.2f元", adminUsername, refundAmount);
        mallAfterSaleTimelineService.addTimeline(
                afterSale.getId(),
                OrderEventTypeEnum.AFTER_SALE_COMPLETED.getType(),
                AfterSaleStatusEnum.COMPLETED.getStatus(),
                OperatorTypeEnum.ADMIN.getType(),
                adminId,
                description
        );

        // 11. 添加订单时间线记录
        OrderTimelineDto orderTimelineDto = OrderTimelineDto.builder()
                .orderId(afterSale.getOrderId())
                .eventType(OrderEventTypeEnum.ORDER_REFUNDED.getType())
                .eventStatus(order.getOrderStatus())
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .description(description)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(orderTimelineDto);

        log.info("管理员{}完成售后退款，售后单号：{}，退款金额：{}", adminUsername, afterSale.getAfterSaleNo(), refundAmount);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processExchange(AfterSaleProcessRequest request) {
        Long adminId = SecurityUtils.getUserId();
        String adminUsername = SecurityUtils.getUsername();

        // 1. 查询售后申请
        MallAfterSale afterSale = getById(request.getAfterSaleId());
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        // 2. 校验售后状态
        AfterSaleStatusEnum afterSaleStatus = afterSale.getAfterSaleStatus();
        if (afterSaleStatus != AfterSaleStatusEnum.APPROVED) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前售后状态[%s]不允许处理换货", afterSaleStatus != null ? afterSaleStatus.getName() : "未知"));
        }

        // 3. 校验售后类型
        AfterSaleTypeEnum afterSaleType = afterSale.getAfterSaleType();
        if (afterSaleType != AfterSaleTypeEnum.EXCHANGE) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "该售后类型不支持换货处理");
        }

        // 4. 更新售后状态为处理中
        Date now = new Date();
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.PROCESSING);
        afterSale.setUpdateTime(now);
        afterSale.setUpdateBy(adminUsername);
        updateById(afterSale);

        // 5. 记录换货物流信息(如果提供)
        String exchangeInfo = "";
        if (request.getLogisticsCompany() != null && request.getTrackingNumber() != null) {
            exchangeInfo = String.format("，换货物流：%s，单号：%s", request.getLogisticsCompany(), request.getTrackingNumber());
        }

        // 6. 更新订单项售后状态为售后完成
        MallOrderItem orderItem = mallOrderItemService.getById(afterSale.getOrderItemId());
        if (orderItem != null) {
            orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.COMPLETED.getStatus());
            orderItem.setUpdateTime(now);
            mallOrderItemService.updateById(orderItem);
        }

        // 7. 更新售后状态为已完成
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.COMPLETED);
        afterSale.setCompleteTime(now);
        afterSale.setUpdateTime(now);
        afterSale.setUpdateBy(adminUsername);
        updateById(afterSale);

        // 8. 添加售后时间线记录
        String description = String.format("管理员%s完成了换货处理%s", adminUsername, exchangeInfo);
        mallAfterSaleTimelineService.addTimeline(
                afterSale.getId(),
                OrderEventTypeEnum.AFTER_SALE_COMPLETED.getType(),
                AfterSaleStatusEnum.COMPLETED.getStatus(),
                OperatorTypeEnum.ADMIN.getType(),
                adminId,
                description
        );

        // 9. 添加订单时间线记录
        MallOrder order = mallOrderService.getById(afterSale.getOrderId());
        OrderTimelineDto orderTimelineDto = OrderTimelineDto.builder()
                .orderId(afterSale.getOrderId())
                .eventType(OrderEventTypeEnum.AFTER_SALE_COMPLETED.getType())
                .eventStatus(order != null ? order.getOrderStatus() : null)
                .operatorType(OperatorTypeEnum.ADMIN.getType())
                .description(description)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(orderTimelineDto);

        log.info("管理员{}完成售后换货，售后单号：{}{}", adminUsername, afterSale.getAfterSaleNo(), exchangeInfo);
        return true;
    }

    /**
     * 处理支付宝退款
     * <p>
     * 注意：不自动降级到钱包退款，避免双重退款风险
     * 当支付宝退款接口异常时，可能的情况：
     * 1. 网络超时 - 退款可能已成功但响应未收到
     * 2. 临时错误 - 支付宝正在处理中
     * 3. 真实失败 - 退款确实失败
     * <p>
     * 在不确定的情况下自动钱包退款会导致双重退款，因此：
     * - 抛出异常，由上层处理
     * - 管理员需要手动查询支付宝退款状态后再决定是否钱包退款
     *
     * @param order        订单信息
     * @param afterSale    售后申请
     * @param refundAmount 退款金额
     */
    private void processAlipayRefund(MallOrder order, MallAfterSale afterSale, BigDecimal refundAmount) {
        try {
            alipayPaymentService.refund(AlipayRefundRequest.builder()
                    .outTradeNo(order.getOrderNo())
                    .refundAmount(formatAmount(refundAmount))
                    .refundReason(String.format("售后退款（售后单号：%s）", afterSale.getAfterSaleNo()))
                    .outRequestNo(buildOutRequestNo(afterSale))
                    .build());
            log.info("支付宝退款成功，售后单号：{}，退款金额：{}", afterSale.getAfterSaleNo(), refundAmount);
        } catch (Exception e) {
            log.error("支付宝退款异常，售后单号：{}，订单号：{}，退款金额：{}，错误信息：{}",
                    afterSale.getAfterSaleNo(), order.getOrderNo(), refundAmount, e.getMessage(), e);
            // 不自动降级到钱包退款，抛出异常由上层处理
            // 管理员需要：
            // 1. 登录支付宝商家后台查询该笔退款状态
            // 2. 如果确认退款失败，可以手动发起钱包退款
            // 3. 如果退款成功或处理中，等待支付宝处理完成
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("支付宝退款失败：%s。请登录支付宝商家后台查询退款状态（售后单号：%s），确认失败后可手动发起钱包退款",
                            e.getMessage(), afterSale.getAfterSaleNo()));
        }
    }

    /**
     * 处理钱包退款
     *
     * @param afterSale    售后申请
     * @param refundAmount 退款金额
     */
    private void processWalletRefund(MallAfterSale afterSale, BigDecimal refundAmount) {
        String walletRemark = String.format("售后退款（售后单号：%s，退款金额：%s元）",
                afterSale.getAfterSaleNo(), formatAmount(refundAmount));
        boolean success = userWalletService.rechargeWallet(afterSale.getUserId(), refundAmount, walletRemark);
        if (!success) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "钱包退款失败");
        }
        log.info("钱包退款成功，售后单号：{}，退款金额：{}", afterSale.getAfterSaleNo(), refundAmount);
    }

    /**
     * 构建支付宝退款的幂等key
     * 使用售后单号作为稳定的幂等键,确保重试时不会产生重复退款
     */
    private String buildOutRequestNo(MallAfterSale afterSale) {
        return afterSale.getAfterSaleNo() + "-REFUND";
    }

    /**
     * 格式化金额为字符串
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

