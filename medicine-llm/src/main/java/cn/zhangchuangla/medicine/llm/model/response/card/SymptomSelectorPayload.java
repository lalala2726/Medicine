package cn.zhangchuangla.medicine.llm.model.response.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 症状选择器卡片载荷。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "症状选择器卡片数据")
public class SymptomSelectorPayload implements CardPayload {

    @Schema(description = "提示文字，引导用户选择/补充症状")
    private String text;

    @Schema(description = "可选症状标签列表")
    private List<String> options;
}
