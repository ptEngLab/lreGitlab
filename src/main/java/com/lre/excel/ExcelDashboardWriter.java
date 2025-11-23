package com.lre.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import java.util.List;

public record ExcelDashboardWriter(Workbook workbook, ExcelStyleFactory styles, ExcelValueWriter valueWriter) {

    private static final int START_ROW = 2;
    private static final int START_COL = 2;
    private static final int GAP = 1;

    private static final int LEFT_COL = START_COL;
    private static final int LEFT_VAL_COL = START_COL + 1;

    private static final int RIGHT_COL = START_COL + 2 + GAP;
    private static final int RIGHT_VAL_COL = START_COL + 3 + GAP;

    public record Section(
            String leftHeader, String rightHeader,
            List<String> leftFields, List<Object> leftValues,
            List<String> rightFields, List<Object> rightValues
    ) {}

    /**
     * Writes a dashboard sheet with sections and their fields/values.
     *
     * @param sheetName the name of the sheet
     * @param sections  the list of sections to write
     */
    public void writeDashboardSheet(String sheetName, List<Section> sections) {
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setDefaultRowHeightInPoints(22);


        int rowNum = START_ROW;

        for (Section section : sections) {
            rowNum = writeSectionHeader(sheet, section, rowNum);
            rowNum = writeSectionData(sheet, section, rowNum);
            rowNum++; // spacing
        }

        ExcelSheetAutoSizer.autoSizeAllColumns(sheet);
    }

    private int writeSectionHeader(Sheet sheet, Section section, int rowNum) {
        Row headerRow = sheet.createRow(rowNum++);
        if (section.leftHeader() != null && !section.leftHeader().isBlank()) {
            createMergedHeader(headerRow, LEFT_COL, LEFT_VAL_COL, section.leftHeader());
        }
        if (section.rightHeader() != null && !section.rightHeader().isBlank()) {
            createMergedHeader(headerRow, RIGHT_COL, RIGHT_VAL_COL, section.rightHeader());
        }
        return rowNum;
    }

    private int writeSectionData(Sheet sheet, Section section, int rowNum) {
        int rows = Math.max(section.leftFields().size(), section.rightFields().size());

        for (int i = 0; i < rows; i++) {
            Row row = sheet.createRow(rowNum++);

            if (i < section.leftFields().size()) {
                writeField(row, LEFT_COL, LEFT_VAL_COL,
                        section.leftFields().get(i),
                        safeValue(section.leftValues(), i));
            }

            if (i < section.rightFields().size()) {
                writeField(row, RIGHT_COL, RIGHT_VAL_COL,
                        section.rightFields().get(i),
                        safeValue(section.rightValues(), i));
            }
        }

        return rowNum;
    }

    private void createMergedHeader(Row row, int startCol, int endCol, String header) {
        Sheet sheet = row.getSheet();

        CellRangeAddress region = new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol);
        sheet.addMergedRegion(region);
        // Apply borders to merged region
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);

        Cell cell = row.createCell(startCol);
        cell.setCellValue(header);
        cell.setCellStyle(styles.getHeaderStyle());
    }

    private void writeField(Row row, int headerCol, int valueCol, String field, Object value) {
        Cell fieldCell = row.createCell(headerCol);
        fieldCell.setCellValue(field != null ? field : "N/A");
        fieldCell.setCellStyle(styles.getPlainDataStyle());

        Cell valueCell = row.createCell(valueCol);
        valueWriter.write(valueCell, value, styles.getPlainDataStyle());
    }

    private Object safeValue(List<Object> values, int index) {
        if (values == null || index >= values.size()) return "N/A";
        Object val = values.get(index);
        return val != null ? val : "N/A";
    }
}
