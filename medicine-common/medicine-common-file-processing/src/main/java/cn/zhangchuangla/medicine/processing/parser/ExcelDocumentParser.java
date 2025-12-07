package cn.zhangchuangla.medicine.processing.parser;

import cn.zhangchuangla.medicine.processing.model.PageTextResult;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 解析（XLS/XLSX），按 Sheet 视作“页”返回文本。
 */
public class ExcelDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String extension) {
        return "xls".equalsIgnoreCase(extension) || "xlsx".equalsIgnoreCase(extension);
    }

    @Override
    public List<PageTextResult> parse(InputStream inputStream, String extension, String fileName) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();
            List<PageTextResult> results = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String text = extractSheet(sheet, formatter);
                if (ParserUtils.hasText(text)) {
                    results.add(PageTextResult.builder()
                            .pageNumber(i + 1)
                            .sectionLabel(sheet.getSheetName())
                            .text(text)
                            .build());
                }
            }
            return results;
        }
    }

    private String extractSheet(Sheet sheet, DataFormatter formatter) {
        StringBuilder builder = new StringBuilder();
        builder.append("Sheet: ").append(sheet.getSheetName()).append('\n');
        for (Row row : sheet) {
            int lastCellNum = row.getLastCellNum();
            boolean rowHasText = false;
            for (int c = 0; c < lastCellNum; c++) {
                String value = formatter.formatCellValue(row.getCell(c));
                if (!value.isBlank()) {
                    rowHasText = true;
                }
                builder.append(value);
                if (c < lastCellNum - 1) {
                    builder.append('\t');
                }
            }
            if (rowHasText) {
                builder.append('\n');
            }
        }
        return ParserUtils.normalize(builder.toString());
    }
}

