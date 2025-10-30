package cn.zhangchuangla.medicine.model.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城商品列表视图对象
 *
 * @author Chuang
 * created on 2025/10/4 02:16
 */
@Data
@Schema(description = "商城商品列表视图对象")
public class MallProductListVo {

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
     * 商品分类名称
     */
    @Schema(description = "商品分类名称", type = "string", example = "保健品")
    private String categoryName;

    /**
     * 商品单位（件、盒、瓶等）
     */
    @Schema(description = "商品单位", type = "string", example = "盒")
    private String unit;

    /**
     * 基础售价
     */
    @Schema(description = "基础售价", type = "number", example = "29.90")
    private BigDecimal price;

    /**
     * 销量
     */
    @Schema(description = "销量", type = "int64", example = "100")
    private Long salesVolume;

    /**
     * 商品库存数量
     */
    @Schema(description = "商品库存数量", type = "int32", example = "50")
    private Integer stock;

    /**
     * 排序值，越小越靠前
     */
    @Schema(description = "排序值，越小越靠前", type = "int32", example = "1")
    private Integer sort;

    /**
     * 状态（1-上架，0-下架）
     */
    @Schema(description = "状态（1-上架，0-下架）", type = "int32", example = "1")
    private Integer status;

    /**
     * 配送方式（快递、自提、同城配送等）
     */
    @Schema(description = "配送方式", type = "int", example = "快递")
    private Integer deliveryType;

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

    /**
     * 创建者
     */
    @Schema(description = "创建者", type = "string", example = "admin")
    private String createBy;

    /**
     * 更新者
     */
    @Schema(description = "更新者", type = "string", example = "admin")
    private String updateBy;

    /**
     * 商品展示图
     */
    @Schema(description = "商品展示图", type = "string", example = "https://example.com/image1.jpg")
    private String image;

}
