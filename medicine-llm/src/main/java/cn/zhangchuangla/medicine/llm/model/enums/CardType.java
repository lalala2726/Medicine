package cn.zhangchuangla.medicine.llm.model.enums;

/**
 *
 * 卡片类型，type=CARD 时必填，如 PRODUCT_LIST、ORDER_STATUS 等
 *
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
public enum CardType {

    SYMPTOM_SELECTOR("symptom-selector"),

    MEDICINE_RECOMMEND("medicine-recommend"),

    PRODUCT_CARD("product-card");

    private final String value;

    CardType(String value) {
        this.value = value;
    }

    public static CardType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CardType cardType : CardType.values()) {
            if (cardType.getValue().equalsIgnoreCase(value)) {
                return cardType;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
