package cn.zhangchuangla.medicine.enums;

/**
 * 医疗工作流状态键枚举
 *
 * @author Chuang
 * @since 2025/9/10
 */
public enum MedicineStateKeyEnum {

    USER_MESSAGE("userMessage", "用户消息"),
    USER_INTENT("userIntent", "用户意图"),
    SYSTEM_RESPONSE("systemResponse", "系统响应");

    private final String key;
    private final String description;

    MedicineStateKeyEnum(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return key;
    }
}
