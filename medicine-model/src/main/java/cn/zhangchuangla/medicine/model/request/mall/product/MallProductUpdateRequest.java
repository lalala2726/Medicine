package cn.zhangchuangla.medicine.model.request.mall.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商城商品修改请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:22
 */
@Data
@Schema(description = "商城商品修改请求对象")
public class MallProductUpdateRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", type = "string", example = "维生素C片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 商品分类ID，关联 mall_category
     */
    @NotNull(message = "商品分类不能为空")
    @Schema(description = "商品分类ID", type = "int64", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    /**
     * 商品单位（件、盒、瓶等）
     */
    @NotBlank(message = "商品单位不能为空")
    @Schema(description = "商品单位", type = "string", example = "盒", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    /**
     * 基础售价
     */
    @NotNull(message = "商品价格不能为空")
    @Schema(description = "基础售价", type = "number", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    /**
     * 商品库存数量
     */
    @Schema(description = "商品库存数量", type = "int32", example = "50")
    private Integer stock;

    /**
     * 排序值，越小越靠前
     */
    @Schema(description = "排序值，越小越靠前", type = "int", example = "1")
    private Integer sort;

    /**
     * 状态（1-上架，0-下架）
     */
    @Schema(description = "状态（1-上架，0-下架）", type = "int32", example = "1")
    private Integer status;

    /**
     * 配送方式（快递、自提、同城配送等）
     */
    @Schema(description = "配送方式（快递、自提、同城配送等）", type = "int", example = "快递")
    @NotNull(message = "配送方式不能为空")
    private Integer deliveryType;

    /**
     * 运费模板ID，关联 mall_product_shipping
     */
    @Schema(description = "运费模板ID", type = "int64", example = "1")
    private Long shippingId;

    /**
     * 商品图片列表
     */
    @Schema(description = "商品图片列表", type = "array", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    @NotEmpty(message = "商品图片列表不能为空")
    private List<String> images;
}
