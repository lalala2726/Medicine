package cn.zhangchuangla.medicine.model.request.mall;

import cn.zhangchuangla.medicine.model.dto.MallMedicineDetailDto;
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
 * created on 2025/10/4
 */
@Data
@Schema(description = "商城商品修改请求对象")
public class MallProductUpdateRequest {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "维生素C片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "商品分类不能为空")
    @Schema(description = "商品分类ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @NotBlank(message = "商品单位不能为空")
    @Schema(description = "商品单位", example = "盒", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @NotNull(message = "商品价格不能为空")
    @Schema(description = "基础售价", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "商品库存数量", example = "50")
    private Integer stock;

    @Schema(description = "排序值，越小越靠前", example = "1")
    private Integer sort;

    @Schema(description = "状态（1-上架，0-下架）", example = "1")
    private Integer status;

    @NotNull(message = "配送方式不能为空")
    @Schema(description = "配送方式（快递、自提、同城配送等）", example = "快递")
    private Integer deliveryType;

    @Schema(description = "运费模板ID", example = "1")
    private Long shippingId;

    @NotEmpty(message = "商品图片列表不能为空")
    @Schema(description = "商品图片列表", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> images;

    @Schema(description = "商品详情")
    private MallMedicineDetailDto medicineDetail;
}
