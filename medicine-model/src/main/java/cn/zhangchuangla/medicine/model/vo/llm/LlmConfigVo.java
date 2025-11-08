package cn.zhangchuangla.medicine.model.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "LLM配置信息视图对象")
@Data
public class LlmConfigVo {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "模型提供商名称", example = "OpenAI")
    private String provider;

    @Schema(description = "模型", example = "gpt-4")
    private String model;

    @Schema(description = "API KEY", example = "sk-xxx")
    private String apiKey;

    @Schema(description = "基础URL", example = "https://api.openai.com/v1")
    private String baseUrl;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-01-01T00:00:00")
    private String createTime;

    @Schema(description = "更新时间", example = "2025-01-01T00:00:00")
    private String updateTime;

    @Schema(description = "创建人", example = "admin")
    private String createBy;

    @Schema(description = "更新人", example = "admin")
    private String updateBy;

}
