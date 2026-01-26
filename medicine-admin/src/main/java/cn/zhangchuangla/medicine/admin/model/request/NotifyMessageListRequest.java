package cn.zhangchuangla.medicine.admin.model.request;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端通知消息分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理端通知消息分页查询请求")
public class NotifyMessageListRequest extends PageRequest {

    @Schema(description = "通知标题(模糊查询)", example = "系统")
    private String title;

    @Schema(description = "通知类型(ORDER/DRUG/SYSTEM)", example = "SYSTEM")
    private String type;

    @Schema(description = "接收者类型(ALL_USER/DESIGNATED_USER)", example = "ALL_USER")
    private String receiverType;

    @Schema(description = "发送者类型(SYSTEM/ADMIN)", example = "ADMIN")
    private String senderType;
}
