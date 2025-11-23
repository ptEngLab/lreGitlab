package com.lre.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Objects;

public record ExcelSheetWriter(Workbook workbook, ExcelStyleFactory styles, ExcelValueWriter valueWriter) {

    /**
     * Write a ResultSet to a sheet with optional merging on a specific column.
     *
     * @param sheetName   the Excel sheet name
     * @param rs          the ResultSet to write
     * @param mergeColumn the column name to merge identical values (nullable)
     */
    public void writeResultSetSheet(String sheetName, ResultSet rs, String mergeColumn) throws Exception {
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setDefaultRowHeightInPoints(22);

        ResultSetMetaData md = rs.getMetaData();
        int colCount = md.getColumnCount();

        writeHeader(sheet, md, colCount);
        sheet.createFreezePane(0, 1); // Freeze header
        Integer mergeIndex = mergeColumn == null ? null : findColumnIndex(md, mergeColumn);
        int lastRow = writeDataRows(sheet, rs, colCount);
        ExcelSheetAutoSizer.autoSizeAllColumns(sheet);
        if (mergeIndex != null) applyMerges(sheet, mergeIndex, lastRow);

    }

    private void writeHeader(Sheet sheet, ResultSetMetaData md, int colCount) throws Exception {
        Row header = sheet.createRow(0);
        for (int i = 1; i <= colCount; i++) {
            Cell c = header.createCell(i - 1);
            c.setCellValue(md.getColumnLabel(i));
            c.setCellStyle(styles.getHeaderStyle());
        }
    }

    /**
     * Writes rows and returns the last row index written.
     */
    private int writeDataRows(Sheet sheet, ResultSet rs, int colCount) throws Exception {
        int rowNum = 1;
        while (rs.next()) {
            Row row = sheet.createRow(rowNum);
            writeRowCells(row, rs, colCount);
            rowNum++;
        }
        return rowNum - 1;
    }

    private void writeRowCells(Row row, ResultSet rs, int colCount) throws Exception {
        for (int col = 1; col <= colCount; col++) {
            Cell cell = row.createCell(col - 1);
            Object value = rs.getObject(col);
            valueWriter.write(cell, value);

            // Optional: wrapped text for long strings
            if (value instanceof String s && s.length() > 40) {
                cell.setCellStyle(styles.getWrappedStyle());
            }
        }
    }

    private void applyMerges(Sheet sheet, int mergeIndex, int lastRow) {
        Object prevMergeValue = null;
        int mergeStart = -1;

        for (int rowNum = 1; rowNum <= lastRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            Object currentVal = row.getCell(mergeIndex - 1).getStringCellValue();

            if (prevMergeValue == null) {
                prevMergeValue = currentVal;
                mergeStart = rowNum;
            } else if (!Objects.equals(prevMergeValue, currentVal)) {
                mergeColumn(sheet, mergeIndex, mergeStart, rowNum - 1);
                prevMergeValue = currentVal;
                mergeStart = rowNum;
            }
        }

        if (mergeStart >= 1) {
            mergeColumn(sheet, mergeIndex, mergeStart, lastRow);
        }
    }

    private void mergeColumn(Sheet sheet, int mergeIndex, int startRow, int endRow) {
        if (endRow > startRow) {
            sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, mergeIndex - 1, mergeIndex - 1));
        }
    }

    private int findColumnIndex(ResultSetMetaData md, String name) throws Exception {
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (md.getColumnLabel(i).equalsIgnoreCase(name)) return i;
        }
        throw new IllegalArgumentException("Column not found: " + name);
    }
}
