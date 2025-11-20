package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallAfterSaleMapper;
import cn.zhangchuangla.medicine.client.model.request.*;
import cn.zhangchuangla.medicine.client.service.MallAfterSaleService;
import cn.zhangchuangla.medicine.client.service.MallAfterSaleTimelineService;
import cn.zhangchuangla.medicine.client.service.MallOrderItemService;
import cn.zhangchuangla.medicine.client.service.MallOrderTimelineService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.dto.OrderTimelineDto;
import cn.zhangchuangla.medicine.model.entity.MallAfterSale;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.*;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleDetailVo;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleListVo;
import cn.zhangchuangla.medicine.model.vo.mall.AfterSaleTimelineVo;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 售后申请Service实现
 *
 * @author Chuang
 * created 2025/11/08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallAfterSaleServiceImpl extends ServiceImpl<MallAfterSaleMapper, MallAfterSale>
        implements MallAfterSaleService, BaseService {

    private final MallAfterSaleMapper mallAfterSaleMapper;
    private final MallAfterSaleTimelineService mallAfterSaleTimelineService;
    private final MallOrderItemService mallOrderItemService;
    private final MallOrderTimelineService mallOrderTimelineService;
    private final cn.zhangchuangla.medicine.client.service.MallOrderService mallOrderService;
    private final cn.zhangchuangla.medicine.client.service.UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyAfterSale(AfterSaleApplyRequest request) {
        Long userId = getUserId();

        // 1. 通过 orderItemId 查询订单项
        MallOrderItem orderItem = mallOrderItemService.getById(request.getOrderItemId());
        if (orderItem == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单项不存在");
        }

        // 2. 通过订单项的 orderId 查询订单
        MallOrder order = mallOrderService.getById(orderItem.getOrderId());
        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 3. 校验订单所属用户
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在");
        }

        // 4. 校验订单状态
        OrderStatusEnum orderStatus = OrderStatusEnum.fromCode(order.getOrderStatus());
        if (orderStatus == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }

        // 只有已完成或待收货、待发货状态可以申请售后
        if (orderStatus != OrderStatusEnum.COMPLETED && orderStatus != OrderStatusEnum.PENDING_RECEIPT
                && orderStatus != OrderStatusEnum.PENDING_SHIPMENT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许申请售后", orderStatus.getName()));
        }

        // 5. 校验订单项售后状态
        String itemAfterSaleStatus = orderItem.getAfterSaleStatus();
        if (itemAfterSaleStatus == null) {
            itemAfterSaleStatus = OrderItemAfterSaleStatusEnum.NONE.getStatus();
        }
        if (!Objects.equals(itemAfterSaleStatus, OrderItemAfterSaleStatusEnum.NONE.getStatus())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "该商品已在售后中，不能重复申请");
        }

        // 6. 校验退款金额
        BigDecimal refundAmount = request.getRefundAmount();
        BigDecimal itemTotalPrice = orderItem.getTotalPrice();
        BigDecimal refundedAmount = orderItem.getRefundedAmount() != null ? orderItem.getRefundedAmount() : BigDecimal.ZERO;
        BigDecimal maxRefundAmount = itemTotalPrice.subtract(refundedAmount);

        if (refundAmount.compareTo(maxRefundAmount) > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("退款金额不能超过可退款金额%.2f元", maxRefundAmount));
        }

        // 7. 组装通用字段
        Date now = new Date();
        String evidenceImagesJson = request.getEvidenceImages() != null && !request.getEvidenceImages().isEmpty()
                ? JSON.toJSONString(request.getEvidenceImages())
                : null;
        ReceiveStatusEnum receiveStatusEnum = ReceiveStatusEnum.fromCode(request.getReceiveStatus());
        if (receiveStatusEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "收货状态异常");
        }

        // 8. 如果存在历史售后记录则复用售后单号与时间线
        MallAfterSale existingAfterSale = lambdaQuery()
                .eq(MallAfterSale::getOrderId, order.getId())
                .eq(MallAfterSale::getOrderItemId, orderItem.getId())
                .eq(MallAfterSale::getUserId, userId)
                .orderByDesc(MallAfterSale::getId)
                .last("limit 1")
                .one();

        MallAfterSale afterSale;
        String afterSaleNo;
        if (existingAfterSale != null) {
            afterSale = existingAfterSale;
            afterSaleNo = existingAfterSale.getAfterSaleNo();
            afterSale.setAfterSaleType(request.getAfterSaleType());
            afterSale.setAfterSaleStatus(AfterSaleStatusEnum.PENDING);
            afterSale.setRefundAmount(refundAmount);
            afterSale.setApplyReason(request.getApplyReason());
            afterSale.setApplyDescription(request.getApplyDescription());
            afterSale.setEvidenceImages(evidenceImagesJson);
            afterSale.setReceiveStatus(receiveStatusEnum);
            afterSale.setApplyTime(now);
            afterSale.setUpdateTime(now);
            afterSale.setUpdateBy(getUsername());

            boolean updated = updateById(afterSale);
            if (!updated) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "申请售后失败，请稍后重试");
            }
        } else {
            afterSaleNo = generateAfterSaleNo();
            afterSale = MallAfterSale.builder()
                    .afterSaleNo(afterSaleNo)
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .orderItemId(orderItem.getId())
                    .userId(userId)
                    .afterSaleType(request.getAfterSaleType())  // 直接使用枚举
                    .afterSaleStatus(AfterSaleStatusEnum.PENDING)
                    .refundAmount(refundAmount)
                    .applyReason(request.getApplyReason())  // 直接使用枚举
                    .applyDescription(request.getApplyDescription())
                    .evidenceImages(evidenceImagesJson)
                    .receiveStatus(receiveStatusEnum)
                    .applyTime(now)
                    .createTime(now)
                    .updateTime(now)
                    .createBy(getUsername())
                    .build();

            boolean saved = save(afterSale);
            if (!saved) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "申请售后失败，请稍后重试");
            }
        }

        // 9. 更新订单项售后状态
        orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.IN_PROGRESS.getStatus());
        orderItem.setUpdateTime(now);
        mallOrderItemService.updateById(orderItem);

        // 10. 更新订单售后标记
        order.setAfterSaleFlag(OrderItemAfterSaleStatusEnum.IN_PROGRESS);
        order.setUpdateTime(now);
        mallOrderService.updateById(order);

        // 11. 添加售后时间线记录
        String username = getUsername();
        AfterSaleTypeEnum afterSaleTypeEnum = afterSale.getAfterSaleType();
        String description = String.format("用户%s申请%s", username,
                afterSaleTypeEnum != null ? afterSaleTypeEnum.getName() : "售后");
        mallAfterSaleTimelineService.addTimeline(
                afterSale.getId(),
                OrderEventTypeEnum.AFTER_SALE_APPLIED.getType(),
                AfterSaleStatusEnum.PENDING.getStatus(),
                OperatorTypeEnum.USER.getType(),
                userId,
                description
        );

        // 12. 添加订单时间线记录
        OrderTimelineDto orderTimelineDto = OrderTimelineDto.builder()
                .orderId(order.getId())
                .eventType(OrderEventTypeEnum.AFTER_SALE_APPLIED.getType())
                .eventStatus(order.getOrderStatus())
                .operatorType(OperatorTypeEnum.USER.getType())
                .description(description)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(orderTimelineDto);

        log.info("用户{}申请售后成功，售后单号：{}", username, afterSaleNo);
        return afterSaleNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> applyOrderRefund(OrderRefundApplyRequest request) {
        Long userId = getUserId();

        MallOrder order = mallOrderService.lambdaQuery()
                .eq(MallOrder::getOrderNo, request.getOrderNo())
                .one();
        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在");
        }

        if (!Objects.equals(order.getPaid(), 1)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单未支付，无法申请退款");
        }

        OrderStatusEnum orderStatusEnum = OrderStatusEnum.fromCode(order.getOrderStatus());
        if (orderStatusEnum == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }
        if (orderStatusEnum != OrderStatusEnum.PENDING_RECEIPT && orderStatusEnum != OrderStatusEnum.COMPLETED
                && orderStatusEnum != OrderStatusEnum.PENDING_SHIPMENT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许整单退款", orderStatusEnum.getName()));
        }

        long activeAfterSale = lambdaQuery()
                .eq(MallAfterSale::getOrderId, order.getId())
                .in(MallAfterSale::getAfterSaleStatus,
                        AfterSaleStatusEnum.PENDING.getStatus(),
                        AfterSaleStatusEnum.APPROVED.getStatus(),
                        AfterSaleStatusEnum.PROCESSING.getStatus())
                .count();
        if (activeAfterSale > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单存在售后处理中，无法再次申请");
        }

        BigDecimal payAmount = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
        BigDecimal refundedAmount = order.getRefundPrice() != null ? order.getRefundPrice() : BigDecimal.ZERO;
        BigDecimal refundableAmount = payAmount.subtract(refundedAmount);
        if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单已无可退款金额");
        }

        List<MallOrderItem> orderItems = mallOrderItemService.lambdaQuery()
                .eq(MallOrderItem::getOrderId, order.getId())
                .list();
        if (orderItems == null || orderItems.isEmpty()) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单商品不存在");
        }

        ReceiveStatusEnum receiveStatusEnum = orderStatusEnum == OrderStatusEnum.COMPLETED
                ? ReceiveStatusEnum.RECEIVED
                : ReceiveStatusEnum.NOT_RECEIVED;

        List<String> afterSaleNos = new ArrayList<>();
        for (MallOrderItem orderItem : orderItems) {
            String itemAfterSaleStatus = orderItem.getAfterSaleStatus();
            if (itemAfterSaleStatus == null) {
                itemAfterSaleStatus = OrderItemAfterSaleStatusEnum.NONE.getStatus();
            }
            if (!Objects.equals(itemAfterSaleStatus, OrderItemAfterSaleStatusEnum.NONE.getStatus())) {
                throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单存在售后中的商品，无法整单退款");
            }

            BigDecimal itemRefunded = orderItem.getRefundedAmount() != null ? orderItem.getRefundedAmount() : BigDecimal.ZERO;
            BigDecimal maxRefundAmount = orderItem.getTotalPrice().subtract(itemRefunded);
            if (maxRefundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            AfterSaleApplyRequest applyRequest = new AfterSaleApplyRequest();
            applyRequest.setOrderItemId(orderItem.getId());
            applyRequest.setAfterSaleType(AfterSaleTypeEnum.REFUND_ONLY);
            applyRequest.setRefundAmount(maxRefundAmount);
            applyRequest.setApplyReason(request.getApplyReason());
            applyRequest.setApplyDescription(request.getApplyDescription());
            applyRequest.setEvidenceImages(request.getEvidenceImages());
            applyRequest.setReceiveStatus(receiveStatusEnum.getStatus());

            String afterSaleNo = applyAfterSale(applyRequest);
            afterSaleNos.add(afterSaleNo);
        }

        if (afterSaleNos.isEmpty()) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单已无可退款金额");
        }

        return afterSaleNos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String reapplyAfterSale(AfterSaleReapplyRequest request) {
        Long userId = getUserId();

        MallAfterSale afterSale = lambdaQuery()
                .eq(MallAfterSale::getAfterSaleNo, request.getAfterSaleNo())
                .one();
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        if (!Objects.equals(afterSale.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "售后申请不存在");
        }

        AfterSaleStatusEnum afterSaleStatusEnum = afterSale.getAfterSaleStatus();
        if (afterSaleStatusEnum != AfterSaleStatusEnum.REJECTED) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前售后状态[%s]不允许再次申请", afterSaleStatusEnum != null ? afterSaleStatusEnum.getName() : "未知"));
        }

        long approvedCount = lambdaQuery()
                .eq(MallAfterSale::getOrderItemId, afterSale.getOrderItemId())
                .eq(MallAfterSale::getUserId, userId)
                .ne(MallAfterSale::getId, afterSale.getId())
                .in(MallAfterSale::getAfterSaleStatus,
                        AfterSaleStatusEnum.APPROVED.getStatus(),
                        AfterSaleStatusEnum.PROCESSING.getStatus(),
                        AfterSaleStatusEnum.COMPLETED.getStatus())
                .count();
        if (approvedCount > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "该商品已存在通过的售后记录，禁止重复申请");
        }

        String evidenceImagesJson = request.getEvidenceImages() != null && !request.getEvidenceImages().isEmpty()
                ? JSON.toJSONString(request.getEvidenceImages())
                : null;

        Date now = new Date();
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.PENDING);
        afterSale.setApplyReason(request.getApplyReason());
        afterSale.setApplyDescription(request.getApplyDescription());
        afterSale.setEvidenceImages(evidenceImagesJson);
        afterSale.setApplyTime(now);
        afterSale.setUpdateTime(now);
        afterSale.setUpdateBy(getUsername());
        afterSale.setRejectReason(null);
        afterSale.setAdminRemark(null);
        afterSale.setAuditTime(null);
        afterSale.setCompleteTime(null);

        boolean updated = updateById(afterSale);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "重新申请售后失败，请稍后重试");
        }

        MallOrderItem orderItem = mallOrderItemService.getById(afterSale.getOrderItemId());
        if (orderItem != null) {
            orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.IN_PROGRESS.getStatus());
            orderItem.setUpdateTime(now);
            mallOrderItemService.updateById(orderItem);
        }

        MallOrder order = mallOrderService.getById(afterSale.getOrderId());
        if (order != null) {
            order.setAfterSaleFlag(OrderItemAfterSaleStatusEnum.IN_PROGRESS);
            order.setUpdateTime(now);
            mallOrderService.updateById(order);
        }

        String username = getUsername();
        String description = String.format("用户%s重新提交售后申请", username);
        mallAfterSaleTimelineService.addTimeline(
                afterSale.getId(),
                OrderEventTypeEnum.AFTER_SALE_APPLIED.getType(),
                AfterSaleStatusEnum.PENDING.getStatus(),
                OperatorTypeEnum.USER.getType(),
                userId,
                description
        );

        OrderTimelineDto orderTimelineDto = OrderTimelineDto.builder()
                .orderId(afterSale.getOrderId())
                .eventType(OrderEventTypeEnum.AFTER_SALE_APPLIED.getType())
                .eventStatus(order != null ? order.getOrderStatus() : null)
                .operatorType(OperatorTypeEnum.USER.getType())
                .description(description)
                .build();
        mallOrderTimelineService.addTimelineIfNotExists(orderTimelineDto);

        log.info("用户{}重新申请售后成功，售后单号：{}", username, afterSale.getAfterSaleNo());
        return afterSale.getAfterSaleNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAfterSale(AfterSaleCancelRequest request) {
        Long userId = getUserId();

        // 1. 查询售后申请
        MallAfterSale afterSale = getById(request.getAfterSaleId());
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        // 校验售后申请所属用户
        if (!Objects.equals(afterSale.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "售后申请不存在");
        }

        // 2. 校验售后状态
        AfterSaleStatusEnum afterSaleStatus = afterSale.getAfterSaleStatus();
        if (afterSaleStatus != AfterSaleStatusEnum.PENDING) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前售后状态[%s]不允许取消", afterSaleStatus != null ? afterSaleStatus.getName() : "未知"));
        }

        // 3. 更新售后状态为已取消
        Date now = new Date();
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.CANCELLED);
        afterSale.setCompleteTime(now);
        afterSale.setUpdateTime(now);
        afterSale.setUpdateBy(getUsername());

        boolean updated = updateById(afterSale);
        if (!updated) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "取消售后失败，请重试");
        }

        // 4. 更新订单项售后状态为无售后
        MallOrderItem orderItem = mallOrderItemService.getById(afterSale.getOrderItemId());
        if (orderItem != null) {
            orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.NONE.getStatus());
            orderItem.setUpdateTime(now);
            mallOrderItemService.updateById(orderItem);
        }

        // 5. 检查订单是否还有其他售后
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

        // 6. 添加售后时间线记录
        String username = getUsername();
        String description = String.format("用户%s取消了售后申请", username);
        if (request.getCancelReason() != null && !request.getCancelReason().trim().isEmpty()) {
            description += String.format("，原因：%s", request.getCancelReason());
        }
        mallAfterSaleTimelineService.addTimeline(
                afterSale.getId(),
                OrderEventTypeEnum.ORDER_CANCELLED.getType(),
                AfterSaleStatusEnum.CANCELLED.getStatus(),
                OperatorTypeEnum.USER.getType(),
                userId,
                description
        );

        log.info("用户{}取消售后成功，售后单号：{}", username, afterSale.getAfterSaleNo());
        return true;
    }

    @Override
    public Page<AfterSaleListVo> getAfterSaleList(AfterSaleListRequest request) {
        Long userId = getUserId();
        Page<AfterSaleListVo> page = request.toPage();
        return mallAfterSaleMapper.selectAfterSaleList(page, request, userId);
    }

    @Override
    public AfterSaleDetailVo getAfterSaleDetail(Long afterSaleId) {
        Long userId = getUserId();

        // 1. 查询售后申请
        MallAfterSale afterSale = getById(afterSaleId);
        if (afterSale == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "售后申请不存在");
        }

        // 校验售后申请所属用户
        if (!Objects.equals(afterSale.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "售后申请不存在");
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

    /**
     * 生成售后单号
     *
     * @return 售后单号
     */
    private String generateAfterSaleNo() {
        String prefix = "AS";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%06d", (int) (Math.random() * 1000000));
        return prefix + datePart + randomPart;
    }
}
