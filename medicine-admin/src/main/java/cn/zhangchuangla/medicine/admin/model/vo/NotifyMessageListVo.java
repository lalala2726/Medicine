package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 管理端通知消息列表视图对象。
 */
@Data
@Schema(description = "管理端通知消息列表视图对象")
public class NotifyMessageListVo {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "通知标题", example = "系统升级通知")
    private String title;

    @Schema(description = "通知类型", example = "SYSTEM")
    private String type;

    @Schema(description = "发送者类型", example = "ADMIN")
    private String senderType;

    @Schema(description = "发送者名称", example = "admin")
    private String senderName;

    @Schema(description = "接收者类型", example = "ALL_USER")
    private String receiverType;

    @Schema(description = "发布时间", example = "2025-01-01 00:00:00")
    private Date publishTime;

    @Schema(description = "创建时间", example = "2025-01-01 00:00:00")
    private Date createTime;
}
