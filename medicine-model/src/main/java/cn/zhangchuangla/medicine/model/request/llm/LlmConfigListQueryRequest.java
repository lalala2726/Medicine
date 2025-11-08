package cn.zhangchuangla.medicine.model.request.llm;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * LLM配置
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LLM配置列表查询参数")
@Data
public class LlmConfigListQueryRequest extends PageRequest {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "模型提供商名称", example = "OpenAI")
    private String provider;

    @Schema(description = "模型", example = "gpt-4")
    private List<String> model;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建人", example = "admin")
    private String createBy;

}
