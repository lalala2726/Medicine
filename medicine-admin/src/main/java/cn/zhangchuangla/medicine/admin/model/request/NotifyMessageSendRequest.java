package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 管理端发送通知消息请求。
 */
@Data
@Schema(description = "管理端发送通知消息请求")
public class NotifyMessageSendRequest {

    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", example = "系统升级通知")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", example = "系统将于今晚 23:00 进行维护升级")
    private String content;

    @NotBlank(message = "通知类型不能为空")
    @Schema(description = "通知类型(ORDER/DRUG/SYSTEM)", example = "SYSTEM")
    private String type;

    @NotBlank(message = "接收者类型不能为空")
    @Schema(description = "接收者类型(ALL_USER/DESIGNATED_USER)", example = "ALL_USER")
    private String receiverType;

    @Schema(description = "发送者名称(不填写默认系统通知)", example = "管理员")
    private String senderName;

    @Schema(description = "指定用户ID列表，仅 DESIGNATED_USER 需要", example = "[1,2,3]")
    private List<Long> receiverIds;

}
