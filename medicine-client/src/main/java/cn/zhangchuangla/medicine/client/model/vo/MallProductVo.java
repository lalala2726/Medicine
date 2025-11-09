package cn.zhangchuangla.medicine.client.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/10/17
 */
@Data
public class MallProductVo {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "商品名称")
    private String name;

    /**
     * 商品单位（件、盒、瓶等）
     */
    @Schema(description = "商品单位（件、盒、瓶等）", example = "件")
    private String unit;

    /**
     * 展示价/兜底价：单规格=唯一SKU价，多规格=最小SKU价；结算以SKU价为准
     */
    @Schema(description = "展示价/兜底价：单规格=唯一SKU价，多规格=最小SKU价；结算以SKU价为准", example = "10.00")
    private BigDecimal price;

    /**
     * 销量
     */
    @Schema(description = "销量", example = "1")
    private Long salesVolume;
}
