package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 通知消息表
 *
 * @TableName notify_message
 */
@TableName(value = "notify_message")
@Data
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
