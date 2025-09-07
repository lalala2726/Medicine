package cn.zhangchuangla.medicine.model.request.llmconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * LLM配置
 */
@Schema(description = "LLM配置添加请求对象")
@Data
public class LlmConfigAddRequest {

    /**
     * 模型提供商名称
     */
    @Schema(description = "模型提供商名称", type = "string", example = "OpenAI")
    private String provider;

    /**
     * 模型
     */
    @Schema(description = "模型", type = "string", example = "gpt-4")
    private String model;

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

    /**
     * 最大Tokens
     */
    @Schema(description = "最大Tokens", type = "int", example = "4096")
    private Integer maxTokens;

    /**
     * 温度
     */
    @Schema(description = "温度", type = "number", example = "0.7")
    private Double temperature;

    /**
     * 是否默认
     */
    @Schema(description = "是否默认", type = "int", example = "0")
    private Integer isDefault;

}