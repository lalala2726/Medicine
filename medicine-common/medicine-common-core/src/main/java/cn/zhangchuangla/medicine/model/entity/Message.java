package cn.zhangchuangla.medicine.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 会话消息表
 */
@TableName(value = "message")
@Data
public class Message {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息UUID
     */
    private String uuid;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 角色类型
     */
    private Object role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 输入token数
     */
    private Integer tokensIn;

    /**
     * 输出token数
     */
    private Integer tokensOut;

    /**
     * 结束原因: stop,length,tool_call等
     */
    private String finishReason;

    /**
     * 额外元信息(JSON),如工具调用参数/结果
     */
    private Object metaJson;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 删除时间
     */
    private Date deleteTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}
