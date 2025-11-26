package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品搜索结果VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MallProductSearchVo {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "商品名称")
    private String productName;

    @Schema(description = "商品封面", example = "https://example.com/product.jpg")
    private String cover;

    @Schema(description = "商品价格", example = "9.99")
    private BigDecimal price;
}
