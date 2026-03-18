package cn.zhangchuangla.medicine.agent.model.vo.client;

import cn.zhangchuangla.medicine.agent.annotation.FieldDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 客户端智能体商品搜索结果。
 */
@Data
@Schema(description = "客户端智能体商品搜索结果")
@FieldDescription(description = "客户端智能体商品搜索结果")
public class ClientAgentProductSearchVo {

    /**
     * 商品ID。
     */
    @Schema(description = "商品ID", example = "1")
    @FieldDescription(description = "商品ID")
    private Long productId;

    /**
     * 商品名称。
     */
    @Schema(description = "商品名称", example = "999感冒灵颗粒")
    @FieldDescription(description = "商品名称")
    private String productName;

    /**
     * 商品封面图。
     */
    @Schema(description = "商品封面图", example = "https://example.com/product.jpg")
    @FieldDescription(description = "商品封面图")
    private String cover;

    /**
     * 商品价格。
     */
    @Schema(description = "商品价格", example = "29.90")
    @FieldDescription(description = "商品价格")
    private BigDecimal price;
}
