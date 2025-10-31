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
     * 支付方式
     */
    private String payType;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 配送方式
     */
    private String deliveryType;

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
