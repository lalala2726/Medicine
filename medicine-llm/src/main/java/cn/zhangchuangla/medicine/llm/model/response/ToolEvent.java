package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.EventType;
import lombok.Builder;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Getter
public class ToolEvent implements EventData {

    private static final Set<EventType> SUPPORTED_EVENT_TYPES =
            EnumSet.of(EventType.TOOL_CALL_START, EventType.TOOL_CALL_END);

    /**
     * 事件类型
     */
    private final EventType eventType;

    /**
     * 工具描述
     */
    private final String description;

    private ToolEvent(EventType eventType, String description) {
        validateEventType(eventType);
        this.eventType = eventType;
        this.description = description;
    }

    @Builder(builderMethodName = "builder")
    public static ToolEvent create(EventType eventType, String description) {
        return new ToolEvent(eventType, description);
    }

    public static ToolEvent start(String description) {
        return new ToolEvent(EventType.TOOL_CALL_START, description);
    }

    public static ToolEvent end(String description) {
        return new ToolEvent(EventType.TOOL_CALL_END, description);
    }

    private static void validateEventType(EventType eventType) {
        if (!SUPPORTED_EVENT_TYPES.contains(eventType)) {
            throw new IllegalArgumentException("只能指定工具的开始和结束");
        }
    }

    @Override
    public EventType eventType() {
        return eventType;
    }
}
