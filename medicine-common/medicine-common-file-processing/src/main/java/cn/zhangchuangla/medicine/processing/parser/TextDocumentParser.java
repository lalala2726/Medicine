package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 纯文本/Markdown 解析。
 */
public class TextDocumentParser implements DocumentParser {

    private static final List<String> SUPPORTED = Arrays.asList("txt", "md");

    @Override
    public boolean supports(String extension) {
        return SUPPORTED.stream().anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }

    @Override
    public List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        text = ParserUtils.normalize(text);
        if (!ParserUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return List.of(PageTextResult.builder()
                .text(text)
                .build());
    }
}

