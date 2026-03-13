package cn.zhangchuangla.medicine.admin.model.request;

import cn.zhangchuangla.medicine.common.core.annotation.PowerOfTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 知识库 Agent 配置请求对象。
 */
@Data
@Schema(description = "知识库Agent配置请求对象")
public class KnowledgeBaseAgentConfigRequest {

    @Schema(description = "可访问知识库名称列表", example = "[\"common_medicine_kb\", \"otc_guide_kb\"]")
    @NotEmpty(message = "知识库名称列表不能为空")
    @Size(max = 5, message = "知识库最多支持5个")
    private List<@NotBlank(message = "知识库名称不能为空") String> knowledgeNames;

    @Schema(description = "向量维度", example = "1024")
    @NotNull(message = "向量维度不能为空")
    @Min(value = 128L, message = "向量维度不能小于128")
    @Max(value = 8192L, message = "向量维度不能大于8192")
    @PowerOfTwo(message = "向量维度必须是2的次方")
    private Integer embeddingDim;

    @Schema(description = "向量模型槽位配置")
    @Valid
    @NotNull(message = "向量模型槽位配置不能为空")
    private AgentModelSelectionRequest embeddingModel;

    @Schema(description = "知识库检索默认返回条数，为空表示使用AI端默认值", example = "10")
    private Integer topK;

    @Schema(description = "是否启用排序", example = "false")
    @NotNull(message = "是否启用排序不能为空")
    private Boolean rankingEnabled;

    @Schema(description = "排序模型槽位配置，可为空表示未配置")
    @Valid
    private AgentModelSelectionRequest rankingModel;
}
