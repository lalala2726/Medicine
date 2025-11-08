package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 
 */
@Data
@Schema(description = "商品推荐视图对象")
public class RecommendListVo {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品封面", example = "https://example.com/product.jpg")
    private String cover;

    @Schema(description = "商品价格", example = "9.99")
    private BigDecimal price;

    @Schema(description = "商品销量", example = "10")
    private Long salesVolume;

}
