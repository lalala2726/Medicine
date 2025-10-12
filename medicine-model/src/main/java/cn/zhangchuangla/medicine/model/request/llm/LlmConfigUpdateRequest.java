package cn.zhangchuangla.medicine.model.request.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * LLM配置
 */
@Schema(description = "LLM配置修改请求对象")
@Data
public class LlmConfigUpdateRequest {

    /**
     * ID
     */
    @Schema(description = "ID", type = "int", format = "int64", example = "1")
    private Long id;

    /**
     * 模型提供商名称
     */
    @Schema(description = "模型提供商名称", type = "string", example = "OpenAI")
    private String provider;

    /**
     * 模型
     */
    @Schema(description = "模型", type = "string", example = "gpt-4")
    private List<String> model;

    /**
     * API KEY
     */
    @Schema(description = "API KEY", type = "string", example = "sk-xxx")
    private String apiKey;

    /**
     * 基础URL
     */
    @Schema(description = "基础URL", type = "string", example = "https://api.openai.com/v1")
    private String baseUrl;

    /**
     * 状态
     */
    @Schema(description = "状态", type = "int", example = "1")
    private Integer status;

}
