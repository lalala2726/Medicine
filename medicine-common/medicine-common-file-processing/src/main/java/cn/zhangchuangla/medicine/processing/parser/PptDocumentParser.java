package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PowerPoint 解析（PPT/PPTX），按幻灯片返回文本。
 */
public class PptDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return "ppt".equalsIgnoreCase(extension) || "pptx".equalsIgnoreCase(extension);
    }

    @Override
    public List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException {
        return "pptx".equalsIgnoreCase(extension)
                ? parsePptx(inputStream)
                : parsePpt(inputStream);
    }

    private List<PageTextResult> parsePptx(InputStream inputStream) throws IOException {
        try (XMLSlideShow show = new XMLSlideShow(inputStream)) {
            List<PageTextResult> results = new ArrayList<>();
            List<XSLFSlide> slides = show.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                XSLFSlide slide = slides.get(i);
                String text = extractText(slide);
                if (ParserUtils.hasText(text)) {
                    results.add(PageTextResult.builder()
                            .pageNumber(i + 1)
                            .sectionLabel(slide.getTitle())
                            .text(text)
                            .build());
                }
            }
            return results;
        }
    }

    private List<PageTextResult> parsePpt(InputStream inputStream) throws IOException {
        try (HSLFSlideShow show = new HSLFSlideShow(inputStream)) {
            List<PageTextResult> results = new ArrayList<>();
            List<HSLFSlide> slides = show.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                HSLFSlide slide = slides.get(i);
                String text = extractText(slide);
                if (ParserUtils.hasText(text)) {
                    results.add(PageTextResult.builder()
                            .pageNumber(i + 1)
                            .sectionLabel(slide.getTitle())
                            .text(text)
                            .build());
                }
            }
            return results;
        }
    }

    private String extractText(XSLFSlide slide) {
        StringBuilder builder = new StringBuilder();
        if (slide.getTitle() != null) {
            builder.append(slide.getTitle()).append('\n');
        }
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape textShape) {
                builder.append(textShape.getText()).append('\n');
            }
        }
        return ParserUtils.normalize(builder.toString());
    }

    private String extractText(HSLFSlide slide) {
        StringBuilder builder = new StringBuilder();
        if (slide.getTitle() != null) {
            builder.append(slide.getTitle()).append('\n');
        }
        for (HSLFShape shape : slide.getShapes()) {
            if (shape instanceof HSLFTextShape textShape) {
                builder.append(textShape.getText()).append('\n');
            }
        }
        return ParserUtils.normalize(builder.toString());
    }
}

