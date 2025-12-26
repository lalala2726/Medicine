package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 系统模块推送通知请求。
 */
@Data
@Schema(description = "系统模块推送通知请求")
public class NotifyMessageSystemPushRequest {

    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", example = "库存预警")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", example = "某商品库存不足，请及时补货")
    private String content;

    @NotBlank(message = "通知类型不能为空")
    @Schema(description = "通知类型(ORDER/DRUG/SYSTEM)", example = "SYSTEM")
    private String type;

    @NotBlank(message = "接收者类型不能为空")
    @Schema(description = "接收者类型(ALL_USER/DESIGNATED_USER)", example = "DESIGNATED_USER")
    private String receiverType;

    @Schema(description = "指定用户ID列表，仅 DESIGNATED_USER 需要", example = "[1,2,3]")
    private List<Long> receiverIds;
}
