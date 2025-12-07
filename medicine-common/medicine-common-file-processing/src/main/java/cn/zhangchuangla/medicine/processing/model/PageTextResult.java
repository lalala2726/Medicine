package cn.zhangchuangla.medicine.processing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 解析后的文本片段，包含可选的分页信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageTextResult {

    /** 页码（从 1 开始），无可靠分页时为 null。 */
    private Integer pageNumber;

    /** 区段标签，可用于标注表格 Sheet 名或幻灯片标题。 */
    private String sectionLabel;

    /** 当前页/区段的纯文本。 */
    private String text;
}

