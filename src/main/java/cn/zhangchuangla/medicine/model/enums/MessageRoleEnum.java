package cn.zhangchuangla.medicine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
@AllArgsConstructor
public enum MessageRoleEnum {
    
    USER("user", "用户"),
    ASSISTANT("assistant", "助手"),
    SYSTEM("system", "系统");
    
    private final String code;
    private final String description;
    
    public static MessageRoleEnum fromCode(String code) {
        for (MessageRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown message role: " + code);
    }
}