package cn.zhangchuangla.medicine.admin.model.vo;

import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商城商品详情视图对象
 *
 * @author Chuang
 * created on 2025/10/4
 */
@Data
@Schema(description = "商城商品详情视图对象")
public class MallProductVo {

    @Schema(description = "商品ID", example = "1")
    private Long id;

    @Schema(description = "商品名称", example = "维生素C片")
    private String name;

    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "商品分类名称", example = "保健品")
    private String categoryName;

    @Schema(description = "商品单位", example = "盒")
    private String unit;

    @Schema(description = "基础售价", example = "29.90")
    private BigDecimal price;

    @Schema(description = "销量", example = "100")
    private Long salesVolume;

    @Schema(description = "商品库存数量", example = "50")
    private Integer stock;

    @Schema(description = "排序值，越小越靠前", example = "1")
    private Integer sort;

    @Schema(description = "状态（1-上架，0-下架）", example = "1")
    private Integer status;

    @Schema(description = "配送方式", example = "快递")
    private Integer deliveryType;

    @Schema(description = "创建时间", example = "2025-01-01 00:00:00")
    private Date createTime;

    @Schema(description = "更新时间", example = "2025-01-01 00:00:00")
    private Date updateTime;

    @Schema(description = "创建者", example = "admin")
    private String createBy;

    @Schema(description = "更新者", example = "admin")
    private String updateBy;

    @Schema(description = "商品图片列表", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> images;

    @Schema(description = "药品说明信息")
    @NotNull(message = "药品说明信息不能为空")
    private DrugDetailDto drugDetail;

}
