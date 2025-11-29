package cn.zhangchuangla.medicine.llm.model.response.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品/订单卡片中的单条记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品或订单卡片条目")
public class ProductCardItem {

    @Schema(description = "商品或订单ID")
    private String id;

    @Schema(description = "展示名称：商品名称或订单摘要")
    private String name;

    @Schema(description = "展示图片URL")
    private String image;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "数量，可为空")
    private Integer quantity;

    @Schema(description = "标签，如状态、下单时间等")
    private List<String> tags;
}
