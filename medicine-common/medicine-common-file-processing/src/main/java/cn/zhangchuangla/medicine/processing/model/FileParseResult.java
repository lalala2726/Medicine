package cn.zhangchuangla.medicine.processing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 单个文件的解析结果汇总。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileParseResult {

    /** 原始文件名（可选）。 */
    private String fileName;

    /** 媒体类型（可选）。 */
    private String mediaType;

    /** 按页/区段拆分的解析文本。 */
    @Builder.Default
    private List<PageTextResult> pages = Collections.emptyList();

    public boolean isEmpty() {
        return pages == null || pages.isEmpty();
    }
}

