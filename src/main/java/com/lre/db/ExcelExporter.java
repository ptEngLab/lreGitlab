package com.lre.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Slf4j
public class ExcelExporter {

    public static void exportToExcel(ResultSet rs, String excelFilePath, String sheetName) throws SQLException {
        createDirectories(excelFilePath);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle defaultStyle = createDefaultStyle(workbook);

            // Create headers
            createHeaders(sheet, metaData, columnCount, headerStyle);

            // Write data rows and track maximum widths
            int[] maxColumnWidths = writeDataRows(rs, sheet, columnCount, dateStyle, numberStyle, defaultStyle);

            // Auto-size columns using efficient method
            autoSizeColumnsOptimized(sheet, columnCount, maxColumnWidths);

            // Write to file
            writeToFile(workbook, excelFilePath);

            log.info("Excel export completed successfully to: {}", excelFilePath);
        } catch (Exception e) {
            log.error("Failed to export to Excel: {}", excelFilePath, e);
            throw new RuntimeException("Excel export failed", e);
        }
    }

    private static void createDirectories(String excelFilePath) {
        try {
            Path filePath = Paths.get(excelFilePath);
            Files.createDirectories(filePath.getParent());
        } catch (Exception e) {
            log.warn("Could not create directories for: {}", excelFilePath, e);
        }
    }

    private static void createHeaders(Sheet sheet, ResultSetMetaData metaData, int columnCount, CellStyle headerStyle) throws SQLException {
        Row headerRow = sheet.createRow(0);
        for (int i = 1; i <= columnCount; i++) {
            Cell cell = headerRow.createCell(i - 1);
            String columnName = metaData.getColumnName(i);
            cell.setCellValue(columnName);
            cell.setCellStyle(headerStyle);
        }
    }

    private static int[] writeDataRows(ResultSet rs, Sheet sheet, int columnCount,
                                       CellStyle dateStyle, CellStyle numberStyle, CellStyle defaultStyle) throws SQLException {
        int[] maxColumnWidths = new int[columnCount];
        int rowNum = 1;
        int processedRows = 0;

        // Initialize with header widths
        Row headerRow = sheet.getRow(0);
        for (int i = 0; i < columnCount; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String value = cell.getStringCellValue();
                maxColumnWidths[i] = Math.max(maxColumnWidths[i], value.length());
            }
        }

        while (rs.next()) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 1; i <= columnCount; i++) {
                Cell cell = row.createCell(i - 1);
                Object value = rs.getObject(i);
                setCellValue(cell, value, dateStyle, numberStyle, defaultStyle);

                String cellValue = getCellStringValue(value);
                maxColumnWidths[i - 1] = Math.max(maxColumnWidths[i - 1], cellValue.length());
            }

            processedRows++;
            if (processedRows % 10000 == 0) {
                log.debug("Processed {} rows for Excel export...", processedRows);
            }
        }

        log.debug("Total rows processed: {}", processedRows);
        return maxColumnWidths;
    }

    private static String getCellStringValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof java.util.Date) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else {
            return value.toString();
        }
    }

    private static void autoSizeColumnsOptimized(Sheet sheet, int columnCount, int[] maxColumnWidths) {
        log.debug("Auto-sizing {} columns...", columnCount);

        for (int i = 0; i < columnCount; i++) {
            try {
                sheet.autoSizeColumn(i);
                int autoSizedWidth = sheet.getColumnWidth(i);
                int contentBasedWidth = calculateColumnWidth(maxColumnWidths[i]);
                int finalWidth = Math.max(autoSizedWidth, contentBasedWidth) + 512; // Add padding
                finalWidth = Math.min(finalWidth, 255 * 256);
                sheet.setColumnWidth(i, finalWidth);
                log.debug("Column {}: content length={} chars, width={} units", i, maxColumnWidths[i], finalWidth);

            } catch (Exception e) {
                log.warn("Auto-size failed for column {}, using manual calculation", i, e);
                // Fallback to manual calculation
                int manualWidth = calculateColumnWidth(maxColumnWidths[i]);
                sheet.setColumnWidth(i, manualWidth);
            }
        }
    }

    private static int calculateColumnWidth(int maxContentLength) {
        int baseWidth = Math.max(maxContentLength, 8) * 256; // Minimum 8 characters
        baseWidth += 1024;
        return Math.min(baseWidth, 255 * 256);
    }

    private static void writeToFile(Workbook workbook, String excelFilePath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
            workbook.write(fos);
        }
    }

    private static CellStyle createCellStyle(Workbook workbook, boolean isDate, boolean isNumber) {
        // Create base style
        CellStyle cellStyle = workbook.createCellStyle();

        // Set common borders
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        // Set alignment
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Set date format if it's a date style
        if (isDate) {
            CreationHelper createHelper = workbook.getCreationHelper();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        }

        // Set number format if it's a number style
        if (isNumber) {
            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        }

        return cellStyle;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        // Create the base style and then customize it for headers
        CellStyle headerStyle = createCellStyle(workbook, false, false);

        // Create header font and apply it to the header style
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        // Set background color for header
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return headerStyle;
    }

    private static CellStyle createDateStyle(Workbook workbook) {
        return createCellStyle(workbook, true, false);
    }

    private static CellStyle createNumberStyle(Workbook workbook) {
        return createCellStyle(workbook, false, true);
    }

    private static CellStyle createDefaultStyle(Workbook workbook) {
        return createCellStyle(workbook, false, false);
    }

    private static void setCellValue(Cell cell, Object value, CellStyle dateStyle, CellStyle numberStyle, CellStyle defaultStyle) {
        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(defaultStyle);
        } else if (value instanceof Number) {
            if (value instanceof Integer || value instanceof Long || value instanceof Short) {
                cell.setCellValue(((Number) value).longValue());
                cell.setCellStyle(defaultStyle);
            } else {
                cell.setCellValue(((Number) value).doubleValue());
                cell.setCellStyle(numberStyle);
            }
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            cell.setCellStyle(defaultStyle);
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(defaultStyle);
        }
    }
}
