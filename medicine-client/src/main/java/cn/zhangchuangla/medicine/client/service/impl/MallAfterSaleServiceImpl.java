package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallAfterSaleMapper;
import cn.zhangchuangla.medicine.client.model.request.AfterSaleApplyRequest;
import cn.zhangchuangla.medicine.client.model.request.AfterSaleCancelRequest;
import cn.zhangchuangla.medicine.client.model.request.AfterSaleListRequest;
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

        // 1. 校验订单和订单项
        MallOrder order = mallOrderService.getById(request.getOrderId());
        if (order == null) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单不存在");
        }

        // 校验订单所属用户
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单信息不存在");
        }

        MallOrderItem orderItem = mallOrderItemService.getById(request.getOrderItemId());
        if (orderItem == null || !Objects.equals(orderItem.getOrderId(), order.getId())) {
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "订单项不存在");
        }

        // 2. 校验订单状态
        OrderStatusEnum orderStatus = OrderStatusEnum.fromCode(order.getOrderStatus());
        if (orderStatus == null) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "订单状态异常");
        }

        // 只有已完成或待收货状态可以申请售后
        if (orderStatus != OrderStatusEnum.COMPLETED && orderStatus != OrderStatusEnum.PENDING_RECEIPT) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前订单状态[%s]不允许申请售后", orderStatus.getName()));
        }

        // 3. 校验订单项售后状态
        String itemAfterSaleStatus = orderItem.getAfterSaleStatus();
        if (itemAfterSaleStatus == null) {
            itemAfterSaleStatus = OrderItemAfterSaleStatusEnum.NONE.getStatus();
        }
        if (!Objects.equals(itemAfterSaleStatus, OrderItemAfterSaleStatusEnum.NONE.getStatus())) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "该商品已在售后中，不能重复申请");
        }

        // 4. 校验退款金额
        BigDecimal refundAmount = request.getRefundAmount();
        BigDecimal itemTotalPrice = orderItem.getTotalPrice();
        BigDecimal refundedAmount = orderItem.getRefundedAmount() != null ? orderItem.getRefundedAmount() : BigDecimal.ZERO;
        BigDecimal maxRefundAmount = itemTotalPrice.subtract(refundedAmount);

        if (refundAmount.compareTo(maxRefundAmount) > 0) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("退款金额不能超过可退款金额%.2f元", maxRefundAmount));
        }

        // 5. 生成售后单号
        String afterSaleNo = generateAfterSaleNo();

        // 6. 创建售后申请记录
        Date now = new Date();
        String evidenceImagesJson = request.getEvidenceImages() != null && !request.getEvidenceImages().isEmpty()
                ? JSON.toJSONString(request.getEvidenceImages())
                : null;

        MallAfterSale afterSale = MallAfterSale.builder()
                .afterSaleNo(afterSaleNo)
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderItemId(orderItem.getId())
                .userId(userId)
                .afterSaleType(request.getAfterSaleType())
                .afterSaleStatus(AfterSaleStatusEnum.PENDING.getStatus())
                .refundAmount(refundAmount)
                .applyReason(request.getApplyReason())
                .applyDescription(request.getApplyDescription())
                .evidenceImages(evidenceImagesJson)
                .receiveStatus(request.getReceiveStatus())
                .applyTime(now)
                .createTime(now)
                .updateTime(now)
                .createBy(getUsername())
                .build();

        boolean saved = save(afterSale);
        if (!saved) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "申请售后失败，请稍后重试");
        }

        // 7. 更新订单项售后状态
        orderItem.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.IN_PROGRESS.getStatus());
        orderItem.setUpdateTime(now);
        mallOrderItemService.updateById(orderItem);

        // 8. 更新订单售后标记
        order.setAfterSaleFlag(1);
        order.setUpdateTime(now);
        mallOrderService.updateById(order);

        // 9. 添加售后时间线记录
        String username = getUsername();
        AfterSaleTypeEnum afterSaleTypeEnum = AfterSaleTypeEnum.fromCode(request.getAfterSaleType());
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

        // 10. 添加订单时间线记录
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
        AfterSaleStatusEnum afterSaleStatus = AfterSaleStatusEnum.fromCode(afterSale.getAfterSaleStatus());
        if (afterSaleStatus != AfterSaleStatusEnum.PENDING) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR,
                    String.format("当前售后状态[%s]不允许取消", afterSaleStatus != null ? afterSaleStatus.getName() : "未知"));
        }

        // 3. 更新售后状态为已取消
        Date now = new Date();
        afterSale.setAfterSaleStatus(AfterSaleStatusEnum.CANCELLED.getStatus());
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
                order.setAfterSaleFlag(0);
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
        AfterSaleTypeEnum afterSaleTypeEnum = AfterSaleTypeEnum.fromCode(afterSale.getAfterSaleType());
        AfterSaleStatusEnum afterSaleStatusEnum = AfterSaleStatusEnum.fromCode(afterSale.getAfterSaleStatus());
        AfterSaleReasonEnum afterSaleReasonEnum = AfterSaleReasonEnum.fromCode(afterSale.getApplyReason());
        ReceiveStatusEnum receiveStatusEnum = ReceiveStatusEnum.fromCode(afterSale.getReceiveStatus());

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
                .afterSaleType(afterSale.getAfterSaleType())
                .afterSaleTypeName(afterSaleTypeEnum != null ? afterSaleTypeEnum.getName() : "未知")
                .afterSaleStatus(afterSale.getAfterSaleStatus())
                .afterSaleStatusName(afterSaleStatusEnum != null ? afterSaleStatusEnum.getName() : "未知")
                .refundAmount(afterSale.getRefundAmount())
                .applyReason(afterSale.getApplyReason())
                .applyReasonName(afterSaleReasonEnum != null ? afterSaleReasonEnum.getName() : "未知")
                .applyDescription(afterSale.getApplyDescription())
                .evidenceImages(evidenceImages)
                .receiveStatus(afterSale.getReceiveStatus())
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

