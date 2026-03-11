package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Agent 模型选择视图对象。
 */
@Data
@Schema(description = "Agent模型选择视图对象")
public class AgentModelSelectionVo {

    @Schema(description = "模型名称", example = "gpt-4.1")
    private String modelName;

    @Schema(description = "是否开启深度思考", example = "true")
    private Boolean reasoningEnabled;

    @Schema(description = "当前模型是否支持深度思考", example = "true")
    private Boolean supportReasoning;

    @Schema(description = "当前模型是否支持图片理解", example = "false")
    private Boolean supportVision;

    @Schema(description = "最大token数", example = "4096")
    private Integer maxTokens;

    @Schema(description = "模型温度", example = "0.7")
    private Double temperature;
}
