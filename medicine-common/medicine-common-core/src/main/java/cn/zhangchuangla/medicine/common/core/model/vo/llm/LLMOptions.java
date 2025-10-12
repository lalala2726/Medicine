package cn.zhangchuangla.medicine.common.core.model.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/9 15:40
 */
@Data
public class LLMOptions {

    /**
     * 模型提供商名称
     */
    @Schema(description = "模型提供商名称", type = "string", example = "OpenAI")
    private String provider;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", type = "string", example = "gpt-4")
    private List<String> model;
}
