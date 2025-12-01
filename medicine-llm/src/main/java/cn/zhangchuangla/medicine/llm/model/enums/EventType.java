package cn.zhangchuangla.medicine.llm.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Getter
public enum EventType {

    OPEN_USER_ORDER_LIST("open_user_order_list");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public static EventType getByValue(String value) {
        for (EventType eventType : EventType.values()) {
            if (eventType.getValue().equals(value)) {
                return eventType;
            }
        }
        return null;
    }

    @JsonValue
    public String jsonValue() {
        return value;
    }

}
