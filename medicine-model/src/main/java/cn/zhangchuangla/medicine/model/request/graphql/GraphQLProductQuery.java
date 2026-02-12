package cn.zhangchuangla.medicine.model.request.graphql;

import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * GraphQL 商品查询对象
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "GraphQL 商品查询对象")
@Data
public class GraphQLProductQuery extends PageRequest {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1")
    @Positive(message = "商品ID必须大于0")
    private Long id;

    /**
     * 商品名称（模糊匹配）
     */
    @Schema(description = "商品名称", example = "维生素C片")
    private String name;

    /**
     * 商品分类ID
     */
    @Schema(description = "商品分类ID", example = "1")
    @Positive(message = "商品分类ID必须大于0")
    private Long categoryId;

    /**
     * 商品状态（1-上架，0-下架）
     */
    @Schema(description = "商品状态（1-上架，0-下架）", example = "1")
    @Min(value = 0, message = "商品状态只能是0或1")
    @Max(value = 1, message = "商品状态只能是0或1")
    private Integer status;

    /**
     * 最低价格
     */
    @Schema(description = "最低价格", example = "10.00")
    @DecimalMin(value = "0.00", message = "最低价格不能小于0")
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    @Schema(description = "最高价格", example = "100.00")
    @DecimalMin(value = "0.00", message = "最高价格不能小于0")
    private BigDecimal maxPrice;
}
