package cn.zhangchuangla.medicine.model.enums;

import lombok.Getter;

/**
 * 订单项售后状态枚举
 *
 * @author Chuang
 * @since 2025/11/08
 */
@Getter
public enum OrderItemAfterSaleStatusEnum {

    /**
     * 无售后
     */
    NONE("NONE", "无售后"),

    /**
     * 售后中
     */
    IN_PROGRESS("IN_PROGRESS", "售后中"),

    /**
     * 售后完成
     */
    COMPLETED("COMPLETED", "售后完成");

    /**
     * 枚举值
     */
    private final String status;

    /**
     * 中文名称
     */
    private final String name;

    OrderItemAfterSaleStatusEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    /**
     * 根据 status 获取枚举
     *
     * @param status 订单项售后状态
     * @return 枚举对象
     */
    public static OrderItemAfterSaleStatusEnum fromCode(String status) {
        if (status == null) {
            return null;
        }
        for (OrderItemAfterSaleStatusEnum itemStatus : values()) {
            if (itemStatus.status.equals(status)) {
                return itemStatus;
            }
        }
        return null;
    }
}

