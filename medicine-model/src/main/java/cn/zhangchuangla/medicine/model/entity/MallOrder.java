package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城订单表（主订单）
 */
@TableName(value = "mall_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MallOrder {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（业务唯一标识）
     */
    private String orderNo;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 订单总金额（含运费）
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

    /**
     * 支付方式（0未支付,1支付宝,2微信,3银行卡）
     */
    private Integer payType;

    /**
     * 订单状态（0待支付,1待发货,2待收货,3已完成,4已退款,5售后中）
     */
    private Integer orderStatus;

    /**
     * 配送方式（1自提,2快递,3同城配送,4药店自送,5冷链,6智能药柜）
     */
    private Integer deliveryType;

    /**
     * 用户收货地址ID
     */
    private Long addressId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货详细地址
     */
    private String receiverDetail;

    /**
     * 用户留言
     */
    private String note;

    /**
     * 是否存在退款申请（0否,1是）
     */
    private Integer refundFlag;

    /**
     * 是否存在售后（0否,1是）
     */
    private Integer afterSaleFlag;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 发货时间
     */
    private Date deliverTime;

    /**
     * 确认收货时间
     */
    private Date receiveTime;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
