package cn.zhangchuangla.medicine.admin.model.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品SKU规格视图对象
 *
 * @author Chuang
 * created on 2025/10/4 02:20
 */
@Data
@Schema(description = "商城商品SKU规格视图对象")
public class MallProductSkuVo {

    /**
     * SKU主键ID
     */
    @Schema(description = "SKU主键ID", type = "int64", example = "1")
    private Long id;

    /**
     * 商品ID，关联 mall_product
     */
    @Schema(description = "商品ID", type = "int64", example = "1")
    private Long productId;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", type = "string", example = "维生素C片")
    private String productName;

    /**
     * 规格名称（如：100ml、两件装）
     */
    @Schema(description = "规格名称", type = "string", example = "100ml")
    private String skuName;

    /**
     * 规格价格
     */
    @Schema(description = "规格价格", type = "number", example = "29.90")
    private BigDecimal price;

    /**
     * 规格库存
     */
    @Schema(description = "规格库存", type = "int32", example = "50")
    private Integer stock;

    /**
     * 规格销量
     */
    @Schema(description = "规格销量", type = "int64", example = "100")
    private Long salesVolume;

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "date", example = "2025-01-01 00:00:00")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", type = "date", example = "2025-01-01 00:00:00")
    private Date updateTime;

}
