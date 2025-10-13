package cn.zhangchuangla.medicine.model.request.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商城商品添加请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:21
 */
@Data
@Schema(description = "商城商品添加请求对象")
public class MallProductAddRequest {

    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
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
    @NotNull(message = "商品单位不能为空")
    @Schema(description = "商品单位", type = "string", example = "盒", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    /**
     * 基础售价
     */
    @NotNull(message = "商品价格不能为空")
    @Schema(description = "基础售价", type = "number", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    /**
     * 独立库存数量（仅当未绑定药品时生效）
     */
    @Schema(description = "独立库存数量", type = "int32", example = "50")
    private Integer stock = 0;

    /**
     * 排序值，越小越靠前
     */
    @Schema(description = "排序值，越小越靠前", type = "int", example = "1")
    private Integer sort = 0;

    /**
     * 状态（1-上架，0-下架）
     */
    @Schema(description = "状态（1-上架，0-下架）", type = "int32", example = "1")
    private Integer status = 1;

    /**
     * 配送方式（快递、自提、同城配送等）
     */
    @Schema(description = "配送方式", type = "string", example = "快递")
    private String deliveryType;

    /**
     * 运费模板ID，关联 mall_product_shipping
     */
    @Schema(description = "运费模板ID", type = "int64", example = "1")
    private Long shippingId;

    /**
     * 库存绑定类型（0-独立库存，1-绑定药品库存）
     */
    @Schema(description = "库存绑定类型（0-独立库存，1-绑定药品库存）", type = "int32", example = "0")
    private Integer bindType = 0;

    /**
     * 关联药品ID（当 bind_type=1 时生效）
     */
    @Schema(description = "关联药品ID", type = "int64", example = "1")
    private Long medicineId;

    /**
     * 关联药品库存批次ID（可选）
     */
    @Schema(description = "关联药品库存批次ID", type = "int64", example = "1")
    private Long medicineStockId;

}
