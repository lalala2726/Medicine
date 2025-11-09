package cn.zhangchuangla.medicine.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * LLM配置
 */
@Schema(description = "LLM配置修改请求对象")
@Data
public class LlmConfigUpdateRequest {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "模型提供商名称", example = "OpenAI")
    private String provider;

    @Schema(description = "模型", example = "gpt-4")
    private List<String> model;

    @Schema(description = "API KEY", example = "sk-xxx")
    private String apiKey;

    @Schema(description = "基础URL", example = "https://api.openai.com/v1")
    private String baseUrl;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
