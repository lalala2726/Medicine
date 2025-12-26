package cn.zhangchuangla.medicine.common.rabbitmq.message;

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

    private String title;
    private String content;
    private String senderType;
    private Long senderId;
    private String senderName;
    private String receiverType;
    private String type;
    private Date publishTime;
    private List<Long> receiverIds;
}
