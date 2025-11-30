package cn.zhangchuangla.medicine.llm.model.response.card;

import cn.zhangchuangla.medicine.llm.model.enums.CardType;
import cn.zhangchuangla.medicine.llm.model.tool.MedicineRecommendPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品推荐卡片，包装卡片类型与载荷。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRecommendCard implements Card {

    private CardType cardType;

    private MedicineRecommendPayload payload;
}
