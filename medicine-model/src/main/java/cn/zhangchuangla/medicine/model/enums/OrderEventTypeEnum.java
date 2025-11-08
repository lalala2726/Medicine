package cn.zhangchuangla.medicine.model.enums;

import lombok.Getter;

/**
 * 订单事件类型枚举
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Getter
public enum OrderEventTypeEnum {

    /**
     * 订单创建
     */
    ORDER_CREATED("ORDER_CREATED", "订单创建", "用户成功创建订单"),

    /**
     * 订单支付
     */
    ORDER_PAID("ORDER_PAID", "订单支付", "订单支付成功"),

    /**
     * 订单发货
     */
    ORDER_SHIPPED("ORDER_SHIPPED", "订单发货", "订单已发货"),

    /**
     * 订单完成
     */
    ORDER_COMPLETED("ORDER_COMPLETED", "订单完成", "订单已完成"),

    /**
     * 订单退款
     */
    ORDER_REFUNDED("ORDER_REFUNDED", "订单退款", "订单已退款"),

    /**
     * 订单取消
     */
    ORDER_CANCELLED("ORDER_CANCELLED", "订单取消", "订单已取消"),

    /**
     * 订单过期
     */
    ORDER_EXPIRED("ORDER_EXPIRED", "订单过期", "订单支付超时已过期"),

    /**
     * 管理员修改地址
     */
    ADMIN_UPDATE_ADDRESS("ADMIN_UPDATE_ADDRESS", "修改地址", "管理员修改了收货地址"),

    /**
     * 管理员修改价格
     */
    ADMIN_UPDATE_PRICE("ADMIN_UPDATE_PRICE", "修改价格", "管理员修改了订单价格"),

    /**
     * 管理员添加备注
     */
    ADMIN_UPDATE_REMARK("ADMIN_UPDATE_REMARK", "添加备注", "管理员添加了订单备注");

    /**
     * 枚举值
     */
    private final String type;

    /**
     * 中文名称
     */
    private final String name;

    /**
     * 描述信息
     */
    private final String description;

    OrderEventTypeEnum(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据 type 获取枚举
     *
     * @param type 事件类型
     * @return 枚举对象
     */
    public static OrderEventTypeEnum fromCode(String type) {
        if (type == null) {
            return null;
        }
        for (OrderEventTypeEnum eventType : values()) {
            if (eventType.type.equals(type)) {
                return eventType;
            }
        }
        return null;
    }
}

