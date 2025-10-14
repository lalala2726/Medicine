package cn.zhangchuangla.medicine.model.request.mall.sku;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商城商品SKU规格修改请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:31
 */
@Data
@Schema(description = "商城商品SKU规格修改请求对象")
public class MallProductSkuUpdateRequest {

    /**
     * SKU主键ID
     */
    @NotNull(message = "SKU ID不能为空")
    @Schema(description = "SKU主键ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 商品ID，关联 mall_product
     */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /**
     * 规格名称（如：100ml、两件装）
     */
    @NotBlank(message = "规格名称不能为空")
    @Schema(description = "规格名称", type = "string", example = "100ml", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuName;

    /**
     * 规格价格
     */
    @NotNull(message = "规格价格不能为空")
    @Schema(description = "规格价格", type = "number", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    /**
     * 规格库存
     */
    @Schema(description = "规格库存", type = "int32", example = "50")
    private Integer stock;

    /**
     * 规格图片URL
     */
    @Schema(description = "规格图片URL", type = "string", example = "https://example.com/sku-image.jpg")
    private String imageUrl;

    /**
     * 排序
     */
    @Schema(description = "排序", type = "int32", example = "1")
    private Integer sort;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态（1-启用，0-禁用）", type = "int32", example = "1")
    private Integer status;

}
