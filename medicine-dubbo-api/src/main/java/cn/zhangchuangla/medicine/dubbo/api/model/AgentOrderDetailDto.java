package cn.zhangchuangla.medicine.dubbo.api.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端智能体订单详情。
 */
@Data
public class AgentOrderDetailDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UserInfo userInfo;

    private DeliveryInfo deliveryInfo;

    private OrderInfo orderInfo;

    private List<ProductInfo> productInfo;

    @Data
    public static class UserInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String userId;

        private String nickname;

        private String phoneNumber;
    }

    @Data
    public static class DeliveryInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String receiverName;

        private String receiverAddress;

        private String receiverPhone;

        private String deliveryMethod;
    }

    @Data
    public static class OrderInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String orderNo;

        private String orderStatus;

        private String payType;

        private BigDecimal totalAmount;

        private BigDecimal payAmount;

        private BigDecimal freightAmount;
    }

    @Data
    public static class ProductInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long productId;

        private String productName;

        private String productImage;

        private BigDecimal productPrice;

        private Integer productQuantity;

        private BigDecimal productTotalAmount;
    }
}
