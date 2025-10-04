package cn.zhangchuangla.medicine.model.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 商城商品详情视图对象
 *
 * @author Chuang
 * created on 2025/10/4 02:17
 */
@Data
@Schema(description = "商城商品详情视图对象")
public class MallProductDetailVo {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", type = "int64", example = "1")
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
     * 商品详情HTML或Markdown内容
     */
    @Schema(description = "商品详情内容", type = "string", example = "<p>商品详细描述...</p>")
    private String content;

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