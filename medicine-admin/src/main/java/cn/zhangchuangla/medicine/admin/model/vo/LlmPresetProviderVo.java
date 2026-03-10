package cn.zhangchuangla.medicine.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预设大模型厂商摘要。
 */
@Data
@Schema(description = "预设大模型厂商摘要")
public class LlmPresetProviderVo {

    @Schema(description = "预设厂商英文键", example = "openai")
    private String providerKey;

    @Schema(description = "提供商名称", example = "OpenAI")
    private String providerName;

    @Schema(description = "基础请求地址", example = "https://api.openai.com/v1")
    private String baseUrl;

    @Schema(description = "描述", example = "OpenAI 官方标准接口预设模板")
    private String description;
}
