package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "LLM配置列表视图对象")
@Data
public class LlmConfigListVo {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "模型提供商名称", example = "OpenAI")
    private String provider;

    @Schema(description = "模型", example = "gpt-4")
    private String model;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-01-01T00:00:00")
    private String createTime;

    @Schema(description = "创建人", example = "admin")
    private String createBy;

}
