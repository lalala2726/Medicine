package cn.zhangchuangla.medicine.model.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 商城商品图片视图对象
 *
 * @author Chuang
 * created on 2025/10/4 02:18
 */
@Data
@Schema(description = "商城商品图片视图对象")
public class MallProductImageVo {

    /**
     * 图片ID
     */
    @Schema(description = "图片ID", type = "int64", example = "1")
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
     * 图片URL
     */
    @Schema(description = "图片URL", type = "string", example = "https://example.com/image.jpg")
    private String imageUrl;

    /**
     * 排序值
     */
    @Schema(description = "排序值", type = "int32", example = "1")
    private Integer sort;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", type = "date", example = "2025-01-01 00:00:00")
    private Date createTime;

}