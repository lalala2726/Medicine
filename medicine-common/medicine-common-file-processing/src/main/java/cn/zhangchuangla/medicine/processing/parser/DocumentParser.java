package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件文本解析器约定。
 */
public interface DocumentParser {

    /**
     * @param extension 小写文件后缀（不含点）
     */
    boolean supports(String extension);

    /**
     * 从输入流解析文本内容。
     *
     * @param inputStream 文件流
     * @param extension   小写后缀（不含点）
     * @param fileName    原始文件名，可空
     * @return 解析后的文本片段
     */
    List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException;
}

