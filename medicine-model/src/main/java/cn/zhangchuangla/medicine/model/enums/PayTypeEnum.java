package cn.zhangchuangla.medicine.model.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 *
 * @author Chuang
 * created 2025/11/01
 */
@Getter
public enum PayTypeEnum {

    /**
     * 钱包
     */
    WALLET("WALLET", "使用钱包余额进行支付"),

    /**
     * 支付宝
     */
    ALIPAY("ALIPAY", "使用支付宝进行支付"),

    /**
     * 微信支付
     */
    WECHAT_PAY("WECHAT_PAY", "使用微信支付进行支付"),

    /**
     * 银行卡
     */
    BANK_CARD("BANK_CARD", "使用银行卡进行支付"),

    /**
     * 待支付
     */
    WAIT_PAY("WAIT_PAY", "待支付"),

    /**
     * 订单已取消
     */
    CANCELLED("CANCELLED", "订单已取消");

    /**
     * 枚举值
     */
    private final String type;


    /**
     * 描述信息
     */
    private final String description;

    PayTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * 根据 type 获取枚举
     */
    public static PayTypeEnum fromCode(String type) {
        if (type == null) {
            return null;
        }
        for (PayTypeEnum payType : values()) {
            if (payType.type.equals(type)) {
                return payType;
            }
        }
        return null;
    }

}
