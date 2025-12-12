package com.lre.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ExcelSheetWriter(Workbook workbook, ExcelStyleFactory styles, ExcelValueWriter valueWriter) {

    /**
     * Writes a list of model objects to a sheet.
     * Uses public getters to generate columns.
     */
    public <T> void writeModelSheet(String sheetName, List<T> models, String mergeColumn) {
        if (models == null || models.isEmpty()) return;

        Sheet sheet = initSheet(sheetName);

        List<Column> columns = extractColumns(models.get(0));
        String[] headers = columns.stream()
                .map(c -> c.field().getName())
                .toArray(String[]::new);

        Integer mergeIndex = findColumnIndex(headers, mergeColumn);

        writeHeaderRow(sheet, headers);
        writeDataRows(sheet, models, columns);

        ExcelSheetAutoSizer.autoSizeAllColumns(sheet);

        if (mergeIndex != null) {
            applyMerges(sheet, mergeIndex, models.size());
        }
    }

    /** Initializes a new sheet with default settings. */
    private Sheet initSheet(String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setDefaultRowHeightInPoints(22);
        sheet.createFreezePane(0, 1);
        return sheet;
    }

    /** Represents a column with its field and getter method. */
    private record Column(Field field, Method getter) {}

    /** Extracts columns (fields with getters) from the model class. */
    private <T> List<Column> extractColumns(T model) {
        return Arrays.stream(model.getClass().getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(f -> {
                    String getterName = "get" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
                    try {
                        Method getter = model.getClass().getMethod(getterName);
                        return new Column(f, getter);
                    } catch (NoSuchMethodException e) {
                        return null; // skip fields without getters
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /** Writes the header row with styles. */
    private void writeHeaderRow(Sheet sheet, String[] headers) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.getHeaderStyle());
        }
    }

    /** Writes model rows using reflection-based getters. */
    private <T> void writeDataRows(Sheet sheet, List<T> models, List<Column> columns) {
        for (int r = 0; r < models.size(); r++) {
            Row row = sheet.createRow(r + 1);
            T model = models.get(r);

            for (int c = 0; c < columns.size(); c++) {
                Cell cell = row.createCell(c);
                try {
                    Object value = columns.get(c).getter().invoke(model);
                    valueWriter.write(cell, value);
                } catch (Exception e) {
                    valueWriter.write(cell, "ERROR");
                }
            }
        }
    }

    /** Applies merges for consecutive identical values in a column. */
    private void applyMerges(Sheet sheet, int mergeIndex, int lastRow) {
        if (mergeIndex < 0) return;

        String prevValue = null;
        int mergeStart = -1;

        for (int rowNum = 1; rowNum <= lastRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;

            String currentVal = Optional.ofNullable(row.getCell(mergeIndex))
                    .map(Cell::getStringCellValue)
                    .orElse("");

            if (prevValue == null) {
                prevValue = currentVal;
                mergeStart = rowNum;
            } else if (!Objects.equals(prevValue, currentVal)) {
                mergeColumn(sheet, mergeIndex, mergeStart, rowNum - 1);
                prevValue = currentVal;
                mergeStart = rowNum;
            }
        }

        if (mergeStart >= 1) {
            mergeColumn(sheet, mergeIndex, mergeStart, lastRow);
        }
    }

    /** Merges cells in a column between startRow and endRow. */
    private void mergeColumn(Sheet sheet, int mergeIndex, int startRow, int endRow) {
        if (endRow > startRow) {
            sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, mergeIndex, mergeIndex));
        }
    }

    /** Finds the index of a column by name (case-insensitive). */
    private Integer findColumnIndex(String[] headers, String columnName) {
        if (columnName == null || headers == null) return null;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return null;
    }
}
