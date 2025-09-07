package cn.zhangchuangla.medicine.model.request.llmconfig;

import cn.zhangchuangla.medicine.common.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM配置
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LLM配置列表查询参数")
@Data
public class LlmConfigListQueryRequest extends BasePageRequest {

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
     * 是否默认
     */
    @Schema(description = "是否默认", type = "int", example = "1")
    private Integer isDefault;

    /**
     * 创建人
     */
    @Schema(description = "创建人", type = "string", example = "admin")
    private String createBy;

}
