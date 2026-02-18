package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.mapper.MallOrderItemMapper;
import cn.zhangchuangla.medicine.agent.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.agent.mapper.MallProductImageMapper;
import cn.zhangchuangla.medicine.agent.mapper.UserMapper;
import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.service.MallOrderService;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallOrderItem;
import cn.zhangchuangla.medicine.model.entity.MallProductImage;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.enums.DeliveryTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MallOrderServiceImpl implements MallOrderService {

    private final MallOrderMapper mallOrderMapper;
    private final MallOrderItemMapper mallOrderItemMapper;
    private final MallProductImageMapper mallProductImageMapper;
    private final UserMapper userMapper;

    @Override
    public Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request) {
        AdminMallOrderListRequest safeRequest = request == null ? new AdminMallOrderListRequest() : request;
        Page<OrderWithProductDto> page = mallOrderMapper.orderListWithProduct(safeRequest.toPage(), safeRequest);
        List<OrderWithProductDto> records = page.getRecords();
        if (records.isEmpty()) {
            return page;
        }

        List<Long> productIds = records.stream()
                .map(OrderWithProductDto::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return page;
        }

        Map<Long, String> coverImageMap = getFirstImageMap(productIds);
        records.forEach(item -> {
            if (item.getProductId() == null || StringUtils.isNotBlank(item.getProductImage())) {
                return;
            }
            item.setProductImage(coverImageMap.get(item.getProductId()));
        });
        return page;
    }

    @Override
    public List<AdminOrderDetailVo> getOrderDetail(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }

        List<MallOrder> orders = mallOrderMapper.selectByIds(orderIds);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = orders.stream()
                .map(MallOrder::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = new LinkedHashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));
        }

        List<Long> normalizedOrderIds = orders.stream().map(MallOrder::getId).toList();
        List<MallOrderItem> orderItems = mallOrderItemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                .in(MallOrderItem::getOrderId, normalizedOrderIds));
        Map<Long, List<MallOrderItem>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(MallOrderItem::getOrderId));

        List<AdminOrderDetailVo> result = new ArrayList<>();
        for (MallOrder order : orders) {
            AdminOrderDetailVo detailVo = new AdminOrderDetailVo();

            User user = userMap.get(order.getUserId());
            if (user != null) {
                AdminOrderDetailVo.UserInfo userInfo = new AdminOrderDetailVo.UserInfo();
                userInfo.setUserId(String.valueOf(user.getId()));
                userInfo.setNickname(user.getNickname());
                userInfo.setPhoneNumber(user.getPhoneNumber());
                detailVo.setUserInfo(userInfo);
            }

            AdminOrderDetailVo.DeliveryInfo deliveryInfo = new AdminOrderDetailVo.DeliveryInfo();
            deliveryInfo.setReceiverName(order.getReceiverName());
            deliveryInfo.setReceiverAddress(order.getReceiverDetail());
            deliveryInfo.setReceiverPhone(order.getReceiverPhone());
            deliveryInfo.setDeliveryMethod(getDeliveryTypeDesc(order.getDeliveryType()));
            detailVo.setDeliveryInfo(deliveryInfo);

            AdminOrderDetailVo.OrderInfo orderInfo = new AdminOrderDetailVo.OrderInfo();
            orderInfo.setOrderNo(order.getOrderNo());
            orderInfo.setOrderStatus(order.getOrderStatus());
            orderInfo.setPayType(order.getPayType());
            orderInfo.setTotalAmount(order.getTotalAmount());
            orderInfo.setPayAmount(order.getPayAmount());
            orderInfo.setFreightAmount(order.getFreightAmount());
            detailVo.setOrderInfo(orderInfo);

            List<AdminOrderDetailVo.ProductInfo> productInfos = new ArrayList<>();
            List<MallOrderItem> currentItems = orderItemMap.getOrDefault(order.getId(), List.of());
            for (MallOrderItem item : currentItems) {
                AdminOrderDetailVo.ProductInfo productInfo = new AdminOrderDetailVo.ProductInfo();
                productInfo.setProductId(item.getProductId());
                productInfo.setProductName(item.getProductName());
                productInfo.setProductImage(item.getImageUrl());
                productInfo.setProductPrice(item.getPrice());
                productInfo.setProductQuantity(item.getQuantity());
                productInfo.setProductTotalAmount(item.getTotalPrice());
                productInfos.add(productInfo);
            }
            detailVo.setProductInfo(productInfos);
            result.add(detailVo);
        }

        return result;
    }

    private Map<Long, String> getFirstImageMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<MallProductImage> images = mallProductImageMapper.selectList(new LambdaQueryWrapper<MallProductImage>()
                .in(MallProductImage::getProductId, productIds)
                .orderByAsc(MallProductImage::getSort));
        if (images.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> imageMap = new LinkedHashMap<>();
        for (MallProductImage image : images) {
            imageMap.putIfAbsent(image.getProductId(), image.getImageUrl());
        }
        return imageMap;
    }

    private String getDeliveryTypeDesc(String deliveryType) {
        DeliveryTypeEnum deliveryTypeEnum = DeliveryTypeEnum.fromCode(deliveryType);
        return deliveryTypeEnum == null ? "未知" : deliveryTypeEnum.getName();
    }
}
