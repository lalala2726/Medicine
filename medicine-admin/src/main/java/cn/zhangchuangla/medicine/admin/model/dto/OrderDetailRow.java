package cn.zhangchuangla.medicine.admin.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单详情批量查询行数据。
 */
@Data
public class OrderDetailRow {

    /**
     * 订单ID。
     */
    private Long orderId;

    /**
     * 订单编号。
     */
    private String orderNo;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 用户昵称。
     */
    private String userNickname;

    /**
     * 用户手机号。
     */
    private String userPhoneNumber;

    /**
     * 收货人姓名。
     */
    private String receiverName;

    /**
     * 收货人手机号。
     */
    private String receiverPhone;

    /**
     * 收货详细地址。
     */
    private String receiverDetail;

    /**
     * 配送方式。
     */
    private String deliveryType;

    /**
     * 订单状态。
     */
    private String orderStatus;

    /**
     * 支付方式。
     */
    private String payType;

    /**
     * 订单总金额。
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额。
     */
    private BigDecimal payAmount;

    /**
     * 运费金额。
     */
    private BigDecimal freightAmount;

    /**
     * 订单项ID。
     */
    private Long orderItemId;

    /**
     * 商品ID。
     */
    private Long productId;

    /**
     * 商品名称。
     */
    private String productName;

    /**
     * 商品图片。
     */
    private String productImage;

    /**
     * 商品单价。
     */
    private BigDecimal productPrice;

    /**
     * 商品数量。
     */
    private Integer productQuantity;

    /**
     * 商品小计金额。
     */
    private BigDecimal productTotalAmount;

    /**
     * 订单创建时间。
     */
    private Date orderCreateTime;
}
