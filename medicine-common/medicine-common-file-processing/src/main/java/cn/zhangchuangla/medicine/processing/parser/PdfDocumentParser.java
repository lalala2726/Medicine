package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 文本解析，按页返回。
 */
public class PdfDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }

    @Override
    public List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException {
        byte[] data = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(data)) {
            int pageCount = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            List<PageTextResult> results = new ArrayList<>(pageCount);
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = ParserUtils.normalize(stripper.getText(document));
                if (ParserUtils.hasText(text)) {
                    results.add(PageTextResult.builder()
                            .pageNumber(page)
                            .text(text)
                            .build());
                }
            }
            return results;
        }
    }
}

