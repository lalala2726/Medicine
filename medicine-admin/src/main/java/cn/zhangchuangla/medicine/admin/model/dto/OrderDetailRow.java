package cn.zhangchuangla.medicine.admin.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单详情批量查询行数据。
 */
@Data
public class OrderDetailRow {

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userNickname;

    private String userPhoneNumber;

    private String receiverName;

    private String receiverPhone;

    private String receiverDetail;

    private String deliveryType;

    private String orderStatus;

    private String payType;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private Long orderItemId;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal productPrice;

    private Integer productQuantity;

    private BigDecimal productTotalAmount;

    private Date orderCreateTime;
}
