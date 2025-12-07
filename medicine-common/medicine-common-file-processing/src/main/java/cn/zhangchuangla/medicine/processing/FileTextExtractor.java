package cn.zhangchuangla.medicine.processing;

import cn.zhangchuangla.medicine.processing.model.FileParseResult;
import cn.zhangchuangla.medicine.processing.model.PageTextResult;
import cn.zhangchuangla.medicine.processing.parser.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 跨格式的统一文本提取入口。
 */
public class FileTextExtractor {

    private final List<DocumentParser> parsers;

    public FileTextExtractor() {
        this(defaultParsers());
    }

    public FileTextExtractor(List<DocumentParser> parsers) {
        this.parsers = List.copyOf(parsers);
    }

    private static List<DocumentParser> defaultParsers() {
        List<DocumentParser> list = new ArrayList<>();
        list.add(new PdfDocumentParser());
        list.add(new WordDocumentParser());
        list.add(new PptDocumentParser());
        list.add(new ExcelDocumentParser());
        list.add(new CsvDocumentParser());
        list.add(new TextDocumentParser());
        return list;
    }

    public FileParseResult parse(String fileName, InputStream inputStream) throws IOException {
        return parse(fileName, null, inputStream);
    }

    public FileParseResult parse(String fileName, String mediaType, InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        String extension = resolveExtension(fileName);
        DocumentParser parser = findParser(extension);
        try (InputStream buffered = new BufferedInputStream(inputStream)) {
            List<PageTextResult> pages = parser.parse(buffered, extension, fileName);
            return FileParseResult.builder()
                    .fileName(fileName)
                    .mediaType(mediaType)
                    .pages(pages == null ? Collections.emptyList() : pages)
                    .build();
        }
    }

    public FileParseResult parseByExtension(String extension, InputStream inputStream) throws IOException {
        return parseByExtension(extension, null, null, inputStream);
    }

    public FileParseResult parseByExtension(String extension, String fileName, String mediaType,
                                            InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        String normalizedExt = normalizeExtension(extension);
        DocumentParser parser = findParser(normalizedExt);
        try (InputStream buffered = new BufferedInputStream(inputStream)) {
            List<PageTextResult> pages = parser.parse(buffered, normalizedExt, fileName);
            return FileParseResult.builder()
                    .fileName(fileName)
                    .mediaType(mediaType)
                    .pages(pages == null ? Collections.emptyList() : pages)
                    .build();
        }
    }

    private DocumentParser findParser(String extension) {
        return parsers.stream()
                .filter(parser -> parser.supports(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file type: " + extension));
    }

    private String resolveExtension(String fileName) {
        if (!ParserUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new IllegalArgumentException("File name with extension is required");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return normalizeExtension(extension);
    }

    private String normalizeExtension(String extension) {
        if (!ParserUtils.hasText(extension)) {
            throw new IllegalArgumentException("Extension is required");
        }
        return extension.toLowerCase(Locale.ROOT).replace(".", "");
    }
}

