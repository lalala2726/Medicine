package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理端编辑通知消息请求。
 */
@Data
@Schema(description = "管理端编辑通知消息请求")
public class NotifyMessageUpdateRequest {

    @NotNull(message = "通知ID不能为空")
    @Schema(description = "通知ID", example = "1")
    private Long id;

    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", example = "系统升级通知")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", example = "系统将于今晚 23:00 进行维护升级")
    private String content;

    @NotBlank(message = "通知类型不能为空")
    @Schema(description = "通知类型(ORDER/DRUG/SYSTEM)", example = "SYSTEM")
    private String type;
}
