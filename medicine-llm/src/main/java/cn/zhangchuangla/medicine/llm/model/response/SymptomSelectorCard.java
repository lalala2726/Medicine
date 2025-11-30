package cn.zhangchuangla.medicine.llm.model.response;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/12/1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SymptomSelectorCard implements Card {

    /**
     * 卡片标题
     */
    private String title;

    /**
     * 卡片类型
     */
    private CardType cardType;

    /**
     * 卡片选项
     */
    private List<String> options;
}
