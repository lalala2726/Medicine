package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户通知消息表
 *
 * @TableName user_notify_message
 */
@TableName(value = "user_notify_message")
@Data
public class UserNotifyMessage {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知ID
     */
    private Long notifyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private Date readTime;

    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;
}
