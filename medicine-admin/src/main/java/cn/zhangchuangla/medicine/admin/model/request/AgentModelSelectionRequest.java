package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Agent 模型选择请求对象。
 */
@Data
@Schema(description = "Agent模型选择请求对象")
public class AgentModelSelectionRequest {

    @Schema(description = "模型名称", example = "gpt-4.1")
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    @Schema(description = "是否开启深度思考", example = "true")
    @NotNull(message = "是否开启深度思考不能为空")
    private Boolean reasoningEnabled;

    @Schema(description = "最大token数", example = "4096")
    @Min(value = 1L, message = "最大token数不能小于1")
    @Max(value = 10000L, message = "最大token数不能大于10000")
    private Integer maxTokens;

    @Schema(description = "模型温度", example = "0.7")
    @DecimalMin(value = "0.0", message = "模型温度不能小于0")
    @DecimalMax(value = "2.0", message = "模型温度不能大于2")
    private Double temperature;
}
