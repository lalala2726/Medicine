package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName(value = "notify_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessage {
    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 发送者类型（SYSTEM | ADMIN)
     */
    private String senderType;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 接收者类型（ALL_USER | DESIGNATED_USER）
     */
    private String receiverType;

    /**
     * 通知类型（ORDER / DRUG / SYSTEM）
     */
    private String type;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;
}
