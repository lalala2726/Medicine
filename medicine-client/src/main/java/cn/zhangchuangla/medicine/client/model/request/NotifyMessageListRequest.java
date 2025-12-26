package cn.zhangchuangla.medicine.client.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户端通知消息列表查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户端通知消息列表查询请求")
public class NotifyMessageListRequest extends PageRequest {

    @Schema(description = "通知标题(模糊查询)", example = "系统")
    private String title;

    @Schema(description = "是否已读(0-未读 1-已读)", example = "0")
    private Integer isRead;
}
