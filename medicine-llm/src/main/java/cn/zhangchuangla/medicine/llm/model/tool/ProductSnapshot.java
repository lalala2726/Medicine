package cn.zhangchuangla.medicine.llm.model.tool;

import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品对话用的精简视图。
 */
@Data
@Schema(description = "商品信息快照")
public class ProductSnapshot {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "状态，1-上架，0-下架")
    private Integer status;

    @Schema(description = "配送方式")
    private Integer deliveryType;

    @Schema(description = "商品图片")
    private List<String> images;

    @Schema(description = "药品详情信息")
    private DrugDetailDto drugDetail;
}
