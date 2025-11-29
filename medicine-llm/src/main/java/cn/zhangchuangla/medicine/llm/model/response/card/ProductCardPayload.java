package cn.zhangchuangla.medicine.llm.model.response.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品/订单卡片载荷。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品或订单卡片数据")
public class ProductCardPayload implements CardPayload {

    @Schema(description = "卡片标题")
    private String title;

    @Schema(description = "商品/订单列表")
    private List<ProductCardItem> products;
}
