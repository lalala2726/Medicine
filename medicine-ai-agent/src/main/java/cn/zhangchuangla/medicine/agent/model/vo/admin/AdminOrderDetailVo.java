package cn.zhangchuangla.medicine.agent.model.vo.admin;

import cn.zhangchuangla.medicine.common.core.annotation.DataMasking;
import cn.zhangchuangla.medicine.common.core.enums.MaskingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin 端智能体订单详情。
 */
@Schema(description = "Admin 端智能体订单详情")
public class AdminOrderDetailVo {

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Schema(description = "配送信息")
    private DeliveryInfo deliveryInfo;

    @Schema(description = "订单信息")
    private OrderInfo orderInfo;

    @Schema(description = "商品信息")
    private List<ProductInfo> productInfo;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }

    public List<ProductInfo> getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(List<ProductInfo> productInfo) {
        this.productInfo = productInfo;
    }

    public static class UserInfo {

        @Schema(description = "用户ID")
        private String userId;

        @Schema(description = "用户昵称")
        private String nickname;

        @Schema(description = "用户手机号")
        @DataMasking(type = MaskingType.MOBILE_PHONE)
        private String phoneNumber;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class DeliveryInfo {

        @Schema(description = "收货人")
        private String receiverName;

        @Schema(description = "收货地址")
        private String receiverAddress;

        @Schema(description = "收货人电话")
        private String receiverPhone;

        @Schema(description = "配送方式")
        private String deliveryMethod;

        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public String getReceiverAddress() {
            return receiverAddress;
        }

        public void setReceiverAddress(String receiverAddress) {
            this.receiverAddress = receiverAddress;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public void setReceiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
        }

        public String getDeliveryMethod() {
            return deliveryMethod;
        }

        public void setDeliveryMethod(String deliveryMethod) {
            this.deliveryMethod = deliveryMethod;
        }
    }

    public static class OrderInfo {

        @Schema(description = "订单编号")
        private String orderNo;

        @Schema(description = "订单状态")
        private String orderStatus;

        @Schema(description = "支付方式")
        private String payType;

        @Schema(description = "订单总金额")
        private BigDecimal totalAmount;

        @Schema(description = "实际支付金额")
        private BigDecimal payAmount;

        @Schema(description = "运费金额")
        private BigDecimal freightAmount;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }

        public String getPayType() {
            return payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getPayAmount() {
            return payAmount;
        }

        public void setPayAmount(BigDecimal payAmount) {
            this.payAmount = payAmount;
        }

        public BigDecimal getFreightAmount() {
            return freightAmount;
        }

        public void setFreightAmount(BigDecimal freightAmount) {
            this.freightAmount = freightAmount;
        }
    }

    public static class ProductInfo {

        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品图片")
        private String productImage;

        @Schema(description = "商品价格")
        private BigDecimal productPrice;

        @Schema(description = "商品数量")
        private Integer productQuantity;

        @Schema(description = "商品总价")
        private BigDecimal productTotalAmount;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductImage() {
            return productImage;
        }

        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }

        public BigDecimal getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(BigDecimal productPrice) {
            this.productPrice = productPrice;
        }

        public Integer getProductQuantity() {
            return productQuantity;
        }

        public void setProductQuantity(Integer productQuantity) {
            this.productQuantity = productQuantity;
        }

        public BigDecimal getProductTotalAmount() {
            return productTotalAmount;
        }

        public void setProductTotalAmount(BigDecimal productTotalAmount) {
            this.productTotalAmount = productTotalAmount;
        }
    }
}
