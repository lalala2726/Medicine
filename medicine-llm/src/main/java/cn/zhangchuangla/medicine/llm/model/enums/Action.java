package cn.zhangchuangla.medicine.llm.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Getter
public enum Action {

    OPEN_USER_ORDER_LIST("open_user_order_list");

    private final String value;

    Action(String value) {
        this.value = value;
    }

    public static Action getByValue(String value) {
        for (Action action : Action.values()) {
            if (action.getValue().equals(value)) {
                return action;
            }
        }
        return null;
    }

    @JsonValue
    public String jsonValue() {
        return value;
    }

}
