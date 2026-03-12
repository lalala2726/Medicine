package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 图片识别 Agent 配置请求对象。
 */
@Data
@Schema(description = "图片识别Agent配置请求对象")
public class ImageRecognitionAgentConfigRequest {

    @Schema(description = "图片识别模型配置")
    @Valid
    @NotNull(message = "图片识别模型槽位配置不能为空")
    private AgentModelSelectionRequest imageRecognitionModel;
}
