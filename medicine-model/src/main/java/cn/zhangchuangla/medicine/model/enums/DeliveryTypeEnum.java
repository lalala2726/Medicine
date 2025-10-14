package cn.zhangchuangla.medicine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送方式枚举
 *
 * @author Chuang
 * @since 2025/10/14
 */
@Getter
@AllArgsConstructor
public enum DeliveryTypeEnum {

    /**
     * 咨询商家
     */
    CONSULT_SELLER(0, "咨询商家", "用户咨询商家进行咨询"),

    /**
     * 用户自提
     */
    SELF_PICKUP(1, "自提", "用户到药店或门店自取"),

    /**
     * 快递配送
     */
    EXPRESS(2, "快递配送", "通过第三方快递公司配送"),

    /**
     * 同城配送
     */
    CITY_DELIVERY(3, "同城配送", "由门店或外包骑手进行同城当日达"),

    /**
     * 药店自送
     */
    DRUG_STORE_DELIVERY(4, "药店自送", "药店自有配送员负责派送"),

    /**
     * 冷链配送
     */
    COLD_CHAIN(5, "冷链配送", "温控运输药品，如疫苗或特殊处方药"),

    /**
     * 智能药柜取药
     */
    PHARMACY_PICKUP_LOCKER(6, "智能药柜取药", "用户扫码到智能药柜自助取药");

    /**
     * 枚举值
     */
    private final Integer code;

    /**
     * 中文名称
     */
    private final String name;

    /**
     * 描述信息
     */
    private final String description;

    /**
     * 根据 code 获取枚举
     */
    public static DeliveryTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeliveryTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
