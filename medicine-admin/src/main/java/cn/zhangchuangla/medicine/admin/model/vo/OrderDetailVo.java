package cn.zhangchuangla.medicine.admin.model.vo;

import cn.zhangchuangla.medicine.common.core.annotation.DataMasking;
import cn.zhangchuangla.medicine.common.core.enums.MaskingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/31 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailVo {

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Schema(description = "配送信息")
    private DeliveryInfo deliveryInfo;

    @Schema(description = "订单信息")
    private OrderInfo orderInfo;

    @Schema(description = "商品信息")
    private List<ProductInfo> productInfo;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {

        @Schema(description = "用户ID")
        private String userId;

        @Schema(description = "用户昵称")
        private String nickname;

        @Schema(description = "用户手机号")
        @DataMasking(type = MaskingType.MOBILE_PHONE)
        private String phoneNumber;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DeliveryInfo {

        @Schema(description = "收货人")
        private String receiverName;

        @Schema(description = "收货地址")
        private String receiverAddress;

        @Schema(description = "收货人电话")
        private String receiverPhone;

        @Schema(description = "配送方式")
        private String deliveryMethod;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
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
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
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
    }


}
