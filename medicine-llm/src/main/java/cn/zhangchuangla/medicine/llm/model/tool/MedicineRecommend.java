package cn.zhangchuangla.medicine.llm.model.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicineRecommend {

    /**
     * 药品ID
     */
    private Long id;

    /**
     * 药品名称
     */
    private String name;

    /**
     * 图片
     */
    private String images;

    /**
     * 价格
     */
    private String price;

    /**
     * 功效
     */
    private String efficacy;

    /**
     * 处方标识
     */
    private Boolean prescription;

    /**
     * 标签
     */
    private String[] tags;

    /**
     * 数量
     */
    private Integer quantity;
}
