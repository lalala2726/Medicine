package cn.zhangchuangla.medicine.admin.ai.enums;

/**
 * 用户意图枚举
 *
 * @author Chuang
 * @since 2025/9/10
 */
public enum UserIntentEnum {

    MEDICINE("medicine", "药品相关"),
    CONSULT("consult", "健康咨询"),
    OTHER("other", "其他问题");

    private final String intent;
    private final String description;

    UserIntentEnum(String intent, String description) {
        this.intent = intent;
        this.description = description;
    }

    public static UserIntentEnum fromString(String intent) {
        if (intent == null) {
            return OTHER;
        }

        String normalizedIntent = intent.trim().toLowerCase();
        for (UserIntentEnum userIntentEnum : values()) {
            if (userIntentEnum.intent.equals(normalizedIntent)) {
                return userIntentEnum;
            }
        }
        return OTHER;
    }

    public String getIntent() {
        return intent;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取对应的节点ID
     */
    public String getNodeId() {
        return intent;
    }

    @Override
    public String toString() {
        return intent;
    }
}
