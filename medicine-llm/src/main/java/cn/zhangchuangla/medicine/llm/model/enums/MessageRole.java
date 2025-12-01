package cn.zhangchuangla.medicine.llm.model.enums;

import lombok.Getter;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
@Getter
public enum MessageRole {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String role;

    MessageRole(String role) {
        this.role = role;
    }

    public static MessageRole fromRole(String role) {
        for (MessageRole value : values()) {
            if (value.role.equals(role)) {
                return value;
            }
        }
        return null;
    }

}
