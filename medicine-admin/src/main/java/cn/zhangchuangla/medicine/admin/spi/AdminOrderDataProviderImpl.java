package cn.zhangchuangla.medicine.admin.spi;

import cn.zhangchuangla.medicine.admin.model.request.MallOrderListRequest;
import cn.zhangchuangla.medicine.admin.model.vo.OrderDetailVo;
import cn.zhangchuangla.medicine.admin.service.MallOrderService;
import cn.zhangchuangla.medicine.agent.model.request.AdminMallOrderListRequest;
import cn.zhangchuangla.medicine.agent.model.vo.admin.AdminOrderDetailVo;
import cn.zhangchuangla.medicine.agent.spi.AdminOrderDataProvider;
import cn.zhangchuangla.medicine.common.core.utils.BeanCotyUtils;
import cn.zhangchuangla.medicine.common.core.utils.SpringUtils;
import cn.zhangchuangla.medicine.model.dto.OrderWithProductDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin 订单数据 SPI 实现。
 */
public class AdminOrderDataProviderImpl implements AdminOrderDataProvider {

    @Override
    public Page<OrderWithProductDto> listOrders(AdminMallOrderListRequest request) {
        MallOrderService mallOrderService = SpringUtils.getBean(MallOrderService.class);
        MallOrderListRequest delegateRequest = BeanCotyUtils.copyProperties(request, MallOrderListRequest.class);
        return mallOrderService.orderWithProduct(delegateRequest);
    }

    @Override
    public List<AdminOrderDetailVo> getOrderDetail(List<Long> orderIds) {
        MallOrderService mallOrderService = SpringUtils.getBean(MallOrderService.class);
        List<OrderDetailVo> details = mallOrderService.getOrderDetailByIds(orderIds);
        return details.stream().map(this::mapOrderDetail).toList();
    }

    private AdminOrderDetailVo mapOrderDetail(OrderDetailVo source) {
        AdminOrderDetailVo target = new AdminOrderDetailVo();
        if (source == null) {
            return target;
        }

        if (source.getUserInfo() != null) {
            AdminOrderDetailVo.UserInfo userInfo = new AdminOrderDetailVo.UserInfo();
            userInfo.setUserId(source.getUserInfo().getUserId());
            userInfo.setNickname(source.getUserInfo().getNickname());
            userInfo.setPhoneNumber(source.getUserInfo().getPhoneNumber());
            target.setUserInfo(userInfo);
        }

        if (source.getDeliveryInfo() != null) {
            AdminOrderDetailVo.DeliveryInfo deliveryInfo = new AdminOrderDetailVo.DeliveryInfo();
            deliveryInfo.setReceiverName(source.getDeliveryInfo().getReceiverName());
            deliveryInfo.setReceiverAddress(source.getDeliveryInfo().getReceiverAddress());
            deliveryInfo.setReceiverPhone(source.getDeliveryInfo().getReceiverPhone());
            deliveryInfo.setDeliveryMethod(source.getDeliveryInfo().getDeliveryMethod());
            target.setDeliveryInfo(deliveryInfo);
        }

        if (source.getOrderInfo() != null) {
            AdminOrderDetailVo.OrderInfo orderInfo = new AdminOrderDetailVo.OrderInfo();
            orderInfo.setOrderNo(source.getOrderInfo().getOrderNo());
            orderInfo.setOrderStatus(source.getOrderInfo().getOrderStatus());
            orderInfo.setPayType(source.getOrderInfo().getPayType());
            orderInfo.setTotalAmount(source.getOrderInfo().getTotalAmount());
            orderInfo.setPayAmount(source.getOrderInfo().getPayAmount());
            orderInfo.setFreightAmount(source.getOrderInfo().getFreightAmount());
            target.setOrderInfo(orderInfo);
        }

        List<AdminOrderDetailVo.ProductInfo> productInfos = new ArrayList<>();
        if (source.getProductInfo() != null) {
            source.getProductInfo().forEach(item -> {
                AdminOrderDetailVo.ProductInfo productInfo = new AdminOrderDetailVo.ProductInfo();
                productInfo.setProductId(item.getProductId());
                productInfo.setProductName(item.getProductName());
                productInfo.setProductImage(item.getProductImage());
                productInfo.setProductPrice(item.getProductPrice());
                productInfo.setProductQuantity(item.getProductQuantity());
                productInfo.setProductTotalAmount(item.getProductTotalAmount());
                productInfos.add(productInfo);
            });
        }
        target.setProductInfo(productInfos);
        return target;
    }
}
