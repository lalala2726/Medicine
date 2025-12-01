package cn.zhangchuangla.medicine.llm.model.tool.client;

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
public class SearchMallProductTool {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 所属分类
     */
    private String categoryName;

    /**
     * 药品通用名
     */
    private String commonName;

    /**
     * 是否处方药
     */
    private Boolean prescription;

    /**
     * 功能主治
     */
    private String efficacy;
}
