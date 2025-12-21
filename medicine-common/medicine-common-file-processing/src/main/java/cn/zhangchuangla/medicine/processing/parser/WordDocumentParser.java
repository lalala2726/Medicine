package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Word 文档解析（DOC/DOCX），不提供精确分页。
 */
public class WordDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return "doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension);
    }

    @Override
    public List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException {
        String text = "docx".equalsIgnoreCase(extension)
                ? parseDocx(inputStream)
                : parseDoc(inputStream);
        text = ParserUtils.normalize(text);
        if (!ParserUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return List.of(PageTextResult.builder()
                .text(text)
                .build());
    }

    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String parseDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
}

