package cn.zhangchuangla.medicine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/16 16:10
 */
@Data
@Schema(description = "商品推荐视图对象")
public class RecommendListVo {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", type = "int", format = "int64", example = "1")
    private Long productId;

    /**
     * 商品封面
     */
    @Schema(description = "商品封面", type = "string", example = "https://example.com/product.jpg")
    private String cover;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", type = "number", example = "9.99")
    private BigDecimal price;

    /**
     * 商品销量
     */
    @Schema(description = "商品销量", type = "int", format = "int64", example = "10")
    private Long salesVolume;

}
