package cn.zhangchuangla.medicine.llm.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Getter
public enum EventType {

    TOOL_CALL_START("tool_call_start"),
    TOOL_CALL_END("tool_call_end");

    private static final Map<String, EventType> VALUE_MAPPING = Map.of(
            TOOL_CALL_START.value, TOOL_CALL_START,
            TOOL_CALL_END.value, TOOL_CALL_END
    );
    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public static Optional<EventType> findByValue(String value) {
        return Optional.ofNullable(VALUE_MAPPING.get(value));
    }

    @Deprecated
    public static EventType getByValue(String value) {
        return VALUE_MAPPING.get(value);
    }

    @JsonValue
    public String jsonValue() {
        return value;
    }
}
