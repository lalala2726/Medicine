package cn.zhangchuangla.medicine.model.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/9 
 */
@Data
public class LLMOptions {

    @Schema(description = "模型提供商名称", example = "OpenAI")
    private String provider;

    @Schema(description = "模型名称", example = "gpt-4")
    private List<String> model;
}
