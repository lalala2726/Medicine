package cn.zhangchuangla.medicine.ai.enums;

import lombok.Getter;

/**
 * 用户意图枚举
 *
 * @author Chuang
 * created 2025/9/10
 */
@Getter
public enum UserIntentEnum {

    MEDICINE("MEDICINE", "药品相关"),
    CONSULT("CONSULT", "健康咨询"),
    OTHER("OTHER", "其他问题");

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
