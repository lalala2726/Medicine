package cn.zhangchuangla.medicine.llm.model.tool;

import cn.zhangchuangla.medicine.model.dto.DrugDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientMallProductOut {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品单位（件、盒、瓶等）
     */
    private String unit;

    /**
     * 展示价
     */
    private BigDecimal price;

    /**
     * 状态（1-上架，0-下架）
     */
    private Integer status;

    /**
     * 配送方式（快递、自提、同城配送等）
     */
    private Integer deliveryType;

    /**
     * 所属分类
     */
    private String categoryName;

    /**
     * 药品详细信息
     */
    private DrugDetailDto drugDetail;

    /**
     * 商品封面
     */
    private String coverImage;
}
