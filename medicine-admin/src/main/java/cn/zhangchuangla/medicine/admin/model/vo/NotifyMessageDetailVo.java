package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 管理端通知消息详情视图对象。
 */
@Data
@Schema(description = "管理端通知消息详情视图对象")
public class NotifyMessageDetailVo {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "通知标题", example = "系统升级通知")
    private String title;

    @Schema(description = "通知内容", example = "系统将于今晚 23:00 进行维护升级")
    private String content;

    @Schema(description = "发送者类型", example = "SYSTEM")
    private String senderType;

    @Schema(description = "发送者ID", example = "0")
    private Long senderId;

    @Schema(description = "发送者名称", example = "系统")
    private String senderName;

    @Schema(description = "接收者类型", example = "ALL_USER")
    private String receiverType;

    @Schema(description = "通知类型", example = "SYSTEM")
    private String type;

    @Schema(description = "指定接收用户ID列表")
    private List<Long> receiverIds;

    @Schema(description = "发布时间", example = "2025-01-01 00:00:00")
    private Date publishTime;

    @Schema(description = "创建时间", example = "2025-01-01 00:00:00")
    private Date createTime;
}
