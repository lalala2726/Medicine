package cn.zhangchuangla.medicine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 客户端智能体商品搜索结果 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客户端智能体商品搜索结果")
public class ClientAgentProductSearchDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID。
     */
    @Schema(description = "商品ID", example = "1")
    private Long productId;

    /**
     * 商品名称。
     */
    @Schema(description = "商品名称", example = "999感冒灵颗粒")
    private String productName;

    /**
     * 商品封面图。
     */
    @Schema(description = "商品封面图", example = "https://example.com/product.jpg")
    private String cover;

    /**
     * 商品价格。
     */
    @Schema(description = "商品价格", example = "29.90")
    private BigDecimal price;
}
