package cn.zhangchuangla.medicine.llm.model.tool.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品/订单卡片中的单条记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品卡片条目")
public class ProductCardItemTool {

    @Schema(description = "商品")
    private String id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "展示图片URL")
    private String image;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;
}
