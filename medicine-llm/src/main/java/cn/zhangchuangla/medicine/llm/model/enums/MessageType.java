package cn.zhangchuangla.medicine.llm.model.enums;

/**
 * 消息类型：TEXT=文本，CARD=卡片，EVENT=事件，ERROR=错误
 *
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
public enum MessageType {

    TEXT("text"),

    CARD("card"),

    EVENT("event"),

    ERROR("error");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public static MessageType fromValue(String value) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getValue().equals(value)) {
                return messageType;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

}
