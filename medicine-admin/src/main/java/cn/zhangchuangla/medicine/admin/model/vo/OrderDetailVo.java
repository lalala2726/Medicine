package cn.zhangchuangla.medicine.admin.model.vo;

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
 * created on 2025/10/31 22:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailVo {

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 配送信息
     */
    @Schema(description = "配送信息")
    private DeliveryInfo deliveryInfo;

    /**
     * 订单信息
     */
    @Schema(description = "订单信息")
    private OrderInfo orderInfo;

    /**
     * 商品信息
     */
    @Schema(description = "商品信息")
    private List<ProductInfo> productInfo;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {

        /**
         * 用户ID
         */
        private String userId;

        /**
         * 用户昵称
         */
        private String nickname;

        /**
         * 用户手机号
         */
        private String phoneNumber;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DeliveryInfo {

        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 收货地址
         */
        private String receiverAddress;

        /**
         * 收货人电话
         */
        private String receiverPhone;

        /**
         * 配送方式
         */
        private String deliveryMethod;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderInfo {

        /**
         * 订单编号
         */
        private String orderNo;

        /**
         * 订单状态
         */
        private String orderStatus;

        /**
         * 支付方式
         */
        private String payType;

        /**
         * 订单总金额
         */
        private BigDecimal totalAmount;

        /**
         * 实际支付金额
         */
        private BigDecimal payAmount;

        /**
         * 运费金额
         */
        private BigDecimal freightAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductInfo {

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品图片
         */
        private String productImage;

        /**
         * 商品价格
         */
        private BigDecimal productPrice;

        /**
         * 商品数量
         */
        private Integer productQuantity;

        /**
         * 商品总价
         */
        private BigDecimal productTotalAmount;
    }


}
