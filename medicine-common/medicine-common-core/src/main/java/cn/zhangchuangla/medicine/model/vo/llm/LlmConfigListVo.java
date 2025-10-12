package cn.zhangchuangla.medicine.model.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * LLM配置
 */
@Schema(description = "LLM配置列表视图对象")
@Data
public class LlmConfigListVo {

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
     * 状态
     */
    @Schema(description = "状态", type = "int", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "string", format = "date-time", example = "2025-01-01T00:00:00")
    private String createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", type = "string", example = "admin")
    private String createBy;

}
