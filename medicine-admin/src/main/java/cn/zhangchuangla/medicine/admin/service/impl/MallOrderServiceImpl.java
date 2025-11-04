package cn.zhangchuangla.medicine.admin.service.impl;

import cn.zhangchuangla.medicine.admin.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.admin.model.request.*;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallOrderItemService;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.admin.service.MallProductImageService;
import cn.zhangchuangla.medicine.admin.service.UserService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.DeliveryTypeEnum;
import cn.zhangchuangla.medicine.model.enums.OrderStatusEnum;
import cn.zhangchuangla.medicine.model.enums.PayTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Chuang
 */
@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder>
        implements MallOrderService {

    private final MallOrderMapper mallOrderMapper;
    private final UserService userService;
    private final MallOrderItemService mallOrderItemService;
    private final MallProductImageService mallProductImageService;


    public MallOrderServiceImpl(MallOrderMapper mallOrderMapper, UserService userService, MallOrderItemService mallOrderItemService, MallProductImageService mallProductImageService) {
        this.mallOrderMapper = mallOrderMapper;
        this.userService = userService;
        this.mallOrderItemService = mallOrderItemService;
        this.mallProductImageService = mallProductImageService;
    }


    @Override
    public Page<MallOrder> orderList(MallOrderListRequest request) {
        Page<MallOrder> mallOrderPage = new Page<>(request.getPageNum(), request.getPageSize());
        return mallOrderMapper.orderList(mallOrderPage, request);
    }

    @Override
    public MallOrder getOrderByOrderNo(String orderNo) {
        Assert.isTrue(orderNo != null, "订单号不能为空");
        LambdaQueryWrapper<MallOrder> mallOrderLambdaQueryWrapper = new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getOrderNo, orderNo);
        MallOrder mallOrder = getOne(mallOrderLambdaQueryWrapper);
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
        User userInfo = userService.getUserById(mallOrder.getUserId());
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
        MallOrder mallOrder = getOrderByOrderNo(request.getOrderNo());

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

        return updateById(mallOrder);
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
        MallOrder mallOrder = getOrderByOrderNo(request.getOrderNo());

        // 更新订单备注
        mallOrder.setNote(request.getRemark());

        return updateById(mallOrder);
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
        MallOrder mallOrder = getOrderByOrderNo(request.getOrderNo());

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

            return updateById(mallOrder);
        } catch (NumberFormatException e) {
            throw new ServiceException(ResponseResultCode.PARAM_ERROR, "价格格式不正确");
        }
    }

    /**
     * 订单退款
     *
     * @param request 订单退款参数
     * @return 是否退款成功
     */
    @Override
    public boolean orderRefund(OrderRefundRequest request) {
        //todo 订单退款待实现
        return false;
    }

    @Override
    public Page<OrderWithProductDto> orderWithProduct(MallOrderListRequest request) {
        Page<OrderWithProductDto> orderWithProductDtoPage = new Page<>(request.getPageNum(), request.getPageSize());
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




