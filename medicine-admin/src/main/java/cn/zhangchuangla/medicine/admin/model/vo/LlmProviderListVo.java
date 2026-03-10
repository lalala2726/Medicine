package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 大模型提供商列表视图对象。
 */
@Data
@Schema(description = "大模型提供商列表视图对象")
public class LlmProviderListVo {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "提供商名称", example = "OpenAI")
    private String providerName;

    @Schema(description = "基础请求地址", example = "https://api.openai.com/v1")
    private String baseUrl;

    @Schema(description = "描述", example = "OpenAI 官方接口")
    private String description;

    @Schema(description = "状态（0启用 1停用）", example = "0")
    private Integer status;

    @Schema(description = "排序值", example = "10")
    private Integer sort;

    @Schema(description = "模型总数", example = "5")
    private Long modelCount;

    @Schema(description = "对话模型数量", example = "3")
    private Long chatModelCount;

    @Schema(description = "重排模型数量", example = "0")
    private Long rerankModelCount;

    @Schema(description = "向量模型数量", example = "2")
    private Long embeddingModelCount;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}
