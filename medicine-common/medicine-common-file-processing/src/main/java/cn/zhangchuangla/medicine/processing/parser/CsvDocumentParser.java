package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * CSV 解析，按整文件返回文本。
 */
public class CsvDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return "csv".equalsIgnoreCase(extension);
    }

    @Override
    public List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException {
        try (CSVParser parser = CSVFormat.DEFAULT.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            for (CSVRecord record : parser) {
                for (int i = 0; i < record.size(); i++) {
                    builder.append(record.get(i));
                    if (i < record.size() - 1) {
                        builder.append('\t');
                    }
                }
                builder.append('\n');
            }
            String text = ParserUtils.normalize(builder.toString());
            if (!ParserUtils.hasText(text)) {
                return Collections.emptyList();
            }
            return List.of(PageTextResult.builder()
                    .text(text)
                    .build());
        }
    }
}

