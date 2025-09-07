package cn.zhangchuangla.medicine.model.vo.llmconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * LLM配置
 */
@Schema(description = "LLM配置信息视图对象")
@Data
public class LlmConfigVo {

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "string", format = "date-time", example = "2025-01-01T00:00:00")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", type = "string", format = "date-time", example = "2025-01-01T00:00:00")
    private String updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", type = "string", example = "admin")
    private String createBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人", type = "string", example = "admin")
    private String updateBy;

}