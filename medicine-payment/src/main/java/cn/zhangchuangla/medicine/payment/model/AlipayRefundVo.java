package cn.zhangchuangla.medicine.payment.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 支付宝退款结果信息。
 */
@Data
@Builder
public class AlipayRefundVo {

    /**
     * 支付宝交易号。
     */
    private String tradeNo;

    /**
     * 商户订单号。
     */
    private String outTradeNo;

    /**
     * 买家在支付宝登录账号。
     */
    private String buyerLogonId;

    /**
     * 退款总金额。
     */
    private String refundFee;

    /**
     * 本次退款是否产生了资金变动。Y 表示有变动。
     */
    private boolean fundChange;

    /**
     * 支付宝退款处理完成时间。
     */
    private Date gmtRefundPay;
}
