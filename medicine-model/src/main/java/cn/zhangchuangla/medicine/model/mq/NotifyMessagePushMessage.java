package cn.zhangchuangla.medicine.model.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 通知消息推送任务消息体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessagePushMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息标题。
     */
    private String title;

    /**
     * 消息内容。
     */
    private String content;

    /**
     * 发送者类型（系统/管理员）。
     */
    private String senderType;

    /**
     * 发送者 ID（系统为 0）。
     */
    private Long senderId;

    /**
     * 发送者名称。
     */
    private String senderName;

    /**
     * 接收者类型（全员/指定用户）。
     */
    private String receiverType;

    /**
     * 消息类型。
     */
    private String type;

    /**
     * 推送时间。
     */
    private Date publishTime;

    /**
     * 接收者用户 ID 列表（仅指定用户时使用）。
     */
    private List<Long> receiverIds;
}
