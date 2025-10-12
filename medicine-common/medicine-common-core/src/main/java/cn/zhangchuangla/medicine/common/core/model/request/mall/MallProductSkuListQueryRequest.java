package cn.zhangchuangla.medicine.common.core.model.request.mall;

import cn.zhangchuangla.medicine.common.core.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商城商品SKU规格列表查询请求对象
 *
 * @author Chuang
 * created on 2025/10/4 14:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商城商品SKU规格列表查询请求对象")
public class MallProductSkuListQueryRequest extends BasePageRequest {

    /**
     * 商品ID，关联 mall_product
     */
    @Schema(description = "商品ID", type = "int64", example = "1")
    private Long productId;

    /**
     * 规格名称
     */
    @Schema(description = "规格名称", type = "string", example = "100ml")
    private String skuName;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态（1-启用，0-禁用）", type = "int32", example = "1")
    private Integer status;

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
