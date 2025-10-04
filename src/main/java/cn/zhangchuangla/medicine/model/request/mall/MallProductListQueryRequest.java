package cn.zhangchuangla.medicine.model.request.mall;

import cn.zhangchuangla.medicine.common.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商城商品列表查询请求对象
 *
 * @author Chuang
 * created on 2025/10/4 02:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商城商品列表查询请求对象")
public class MallProductListQueryRequest extends BasePageRequest {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", type = "int64", example = "1")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", type = "string", example = "维生素C片")
    private String name;

    /**
     * 商品分类ID，关联 mall_category
     */
    @Schema(description = "商品分类ID", type = "int64", example = "1")
    private Long categoryId;

    /**
     * 状态（1-上架，0-下架）
     */
    @Schema(description = "状态（1-上架，0-下架）", type = "int32", example = "1")
    private Integer status;

    /**
     * 库存绑定类型（0-独立库存，1-绑定药品库存）
     */
    @Schema(description = "库存绑定类型（0-独立库存，1-绑定药品库存）", type = "int32", example = "0")
    private Integer bindType;

    /**
     * 关联药品ID（当 bind_type=1 时生效）
     */
    @Schema(description = "关联药品ID", type = "int64", example = "1")
    private Long medicineId;

    /**
     * 最低价格
     */
    @Schema(description = "最低价格", type = "number", example = "10.00")
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    @Schema(description = "最高价格", type = "number", example = "100.00")
    private BigDecimal maxPrice;

}