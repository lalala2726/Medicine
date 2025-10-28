package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/17 13:42
 */
@Data
public class MallProductVo {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", type = "int", format = "int64", example = "1")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", type = "string", example = "商品名称")
    private String name;

    /**
     * 商品单位（件、盒、瓶等）
     */
    @Schema(description = "商品单位（件、盒、瓶等）", type = "string", example = "件")
    private String unit;

    /**
     * 展示价/兜底价：单规格=唯一SKU价，多规格=最小SKU价；结算以SKU价为准
     */
    @Schema(description = "展示价/兜底价：单规格=唯一SKU价，多规格=最小SKU价；结算以SKU价为准", type = "number", example = "10.00")
    private BigDecimal price;

    /**
     * 销量
     */
    @Schema(description = "销量", type = "int", format = "int64", example = "1")
    private Long salesVolume;


}
