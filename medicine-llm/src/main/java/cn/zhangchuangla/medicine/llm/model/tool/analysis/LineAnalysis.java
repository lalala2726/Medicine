package cn.zhangchuangla.medicine.llm.model.tool.analysis;

import lombok.Data;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/24
 */
@Data
public class LineAnalysis {

    /**
     * 时间
     */
    private String time;

    /**
     * 值
     */
    private Integer value;

    /**
     * 分组
     */
    private String group;
}
