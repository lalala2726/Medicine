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
    @Schema(description = "用户信息", example = "{\"userId\":\"1\",\"nickname\":\"张三\",\"phoneNumber\":\"138****0000\"}")
    private UserInfo userInfo;

    /**
     * 配送信息
     */
    @Schema(description = "配送信息", example = "{\"receiverName\":\"张三\",\"receiverAddress\":\"中国北京市海淀区\",\"receiverPhone\":\"13800000000\",\"deliveryMethod\":\"顺丰\"}")
    private DeliveryInfo deliveryInfo;

    /**
     * 订单信息
     */
    @Schema(description = "订单信息", example = "{\"orderNo\":\"O2025103123456789\",\"orderStatus\":\"待付款\",\"payType\":\"支付宝\",\"totalAmount\":10.00,\"payAmount\":10.00,\"freightAmount\":0.00}")
    private OrderInfo orderInfo;

    /**
     * 商品信息
     */
    @Schema(description = "商品信息", example = "[{\"productId\":1,\"productName\":\"商品名称\",\"productImage\":\"https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png\",\"productPrice\":10.00,\"productQuantity\":1,\"productTotalAmount\":10.00}]")
    private List<ProductInfo> productInfo;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1")
        private String userId;

        /**
         * 用户昵称
         */
        @Schema(description = "用户昵称", example = "张三")
        private String nickname;

        /**
         * 用户手机号
         */
        @Schema(description = "用户手机号", example = "13800000000")
        @DataMasking(type = MaskingType.MOBILE_PHONE)
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
        @Schema(description = "收货人", example = "张三")
        private String receiverName;

        /**
         * 收货地址
         */
        @Schema(description = "收货地址", example = "中国北京市海淀区")
        private String receiverAddress;

        /**
         * 收货人电话
         */
        @Schema(description = "收货人电话", example = "13800000000")
        private String receiverPhone;

        /**
         * 配送方式
         */
        @Schema(description = "配送方式", example = "顺丰")
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
        @Schema(description = "订单编号", example = "O2025103123456789")
        private String orderNo;

        /**
         * 订单状态
         */
        @Schema(description = "订单状态", example = "待付款")
        private String orderStatus;

        /**
         * 支付方式
         */
        @Schema(description = "支付方式", example = "支付宝")
        private String payType;

        /**
         * 订单总金额
         */
        @Schema(description = "订单总金额", example = "10.00")
        private BigDecimal totalAmount;

        /**
         * 实际支付金额
         */
        @Schema(description = "实际支付金额", example = "10.00")
        private BigDecimal payAmount;

        /**
         * 运费金额
         */
        @Schema(description = "运费金额", example = "10.00")
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
        @Schema(description = "商品ID", example = "1")
        private Long productId;

        /**
         * 商品名称
         */
        @Schema(description = "商品名称", example = "商品名称")
        private String productName;

        /**
         * 商品图片
         */
        @Schema(description = "商品图片", example = "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
        private String productImage;

        /**
         * 商品价格
         */
        @Schema(description = "商品价格", example = "10.00")
        private BigDecimal productPrice;

        /**
         * 商品数量
         */
        @Schema(description = "商品数量", example = "1")
        private Integer productQuantity;

        /**
         * 商品总价
         */
        @Schema(description = "商品总价", example = "10.00")
        private BigDecimal productTotalAmount;
    }


}
