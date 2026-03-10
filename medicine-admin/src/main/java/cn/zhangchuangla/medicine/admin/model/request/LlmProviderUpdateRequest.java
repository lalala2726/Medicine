package cn.zhangchuangla.medicine.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 编辑大模型提供商请求。
 */
@Data
@Schema(description = "编辑大模型提供商请求")
public class LlmProviderUpdateRequest {

    @Schema(description = "主键ID", example = "1")
    @NotNull(message = "提供商ID不能为空")
    @Positive(message = "提供商ID必须大于0")
    private Long id;

    @Schema(description = "预设厂商英文键，仅用于前端模板标识", example = "openai")
    private String providerKey;

    @Schema(description = "提供商名称", example = "OpenAI")
    private String providerName;

    @Schema(description = "基础请求地址", example = "https://api.openai.com/v1")
    private String baseUrl;

    @Schema(description = "提供商描述", example = "OpenAI 官方接口")
    private String description;

    @Schema(description = "状态（0启用 1停用）", example = "0")
    @Min(value = 0L, message = "状态值不合法")
    @Max(value = 1L, message = "状态值不合法")
    private Integer status;

    @Schema(description = "排序值，值越小越靠前", example = "10")
    private Integer sort;

    @Schema(description = "模型列表，编辑时按整组替换处理")
    @NotEmpty(message = "模型列表不能为空")
    private List<@NotNull(message = "模型信息不能为空") @Valid LlmProviderModelItemRequest> models;
}
