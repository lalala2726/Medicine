package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 客户端通知消息列表视图对象。
 */
@Data
@Schema(description = "客户端通知消息列表视图对象")
public class NotifyMessageListVo {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "通知标题", example = "系统升级通知")
    private String title;

    @Schema(description = "内容概述", example = "系统将于今晚 23:00")
    private String contentSummary;

    @Schema(description = "发布时间", example = "2025-01-01 00:00:00")
    private Date publishTime;

    @Schema(description = "是否已读(0-未读 1-已读)", example = "0")
    private Integer isRead;
}
