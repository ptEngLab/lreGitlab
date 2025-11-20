package com.lre.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Slf4j
public class SheetWriter {
    private final ResultSet rs;
    private final Sheet sheet;
    private final ResultSetMetaData md;
    private final SheetStyler styler;
    private final MergeCollector mergeCollector;
    private final int colCount;

    SheetWriter(ResultSet rs, Sheet sheet, ResultSetMetaData md, SheetStyler styler, String mergeColumn) 
            throws SQLException {
        this.rs = rs;
        this.sheet = sheet;
        this.md = md;
        this.styler = styler;
        this.colCount = md.getColumnCount();
        this.mergeCollector = mergeColumn != null ? new MergeCollector(md, mergeColumn) : null;
    }

    void writeData() throws SQLException {
        createHeaderRow();
        int[] maxWidths = initHeaderWidths();
        
        int rowNum = 1;
        while (rs.next()) {
            Row row = sheet.createRow(rowNum);
            writeRowData(row, maxWidths);
            
            if (mergeCollector != null) {
                mergeCollector.track(rs, rowNum);
            }
            rowNum++;
        }

        if (mergeCollector != null) {
            mergeCollector.apply(sheet);
        }

        autoSizeColumns(maxWidths);
    }

    private void createHeaderRow() throws SQLException {
        Row row = sheet.createRow(0);
        for (int i = 1; i <= colCount; i++) {
            Cell cell = row.createCell(i - 1);
            cell.setCellValue(md.getColumnName(i));
            cell.setCellStyle(styler.getHeaderStyle());
        }
    }

    private int[] initHeaderWidths() {
        int[] widths = new int[colCount];
        Row headerRow = sheet.getRow(0);
        
        for (int i = 0; i < colCount; i++) {
            Cell cell = headerRow.getCell(i);
            widths[i] = (cell != null) ? cell.getStringCellValue().length() : 8;
        }
        return widths;
    }

    private void writeRowData(Row row, int[] maxWidths) throws SQLException {
        for (int c = 0; c < colCount; c++) {
            String colName = md.getColumnName(c + 1).toLowerCase();
            Object value = rs.getObject(c + 1);

            Cell cell = row.createCell(c);
            setTypedValue(cell, value, styler.getColumnStyle(colName));

            maxWidths[c] = Math.max(maxWidths[c], value == null ? 0 : value.toString().length());
        }
    }

    private void autoSizeColumns(int[] maxWidths) {
        for (int i = 0; i < colCount; i++) {
            int width = Math.min((maxWidths[i] + 3) * 256, 255 * 256);
            width = Math.max(width, 8 * 256);
            sheet.setColumnWidth(i, width);
        }
    }

    private void setTypedValue(Cell cell, Object value, CellStyle style) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number n) {
            if (n instanceof Integer || n instanceof Long || n instanceof Short || n instanceof Byte) {
                cell.setCellValue(n.longValue());
            } else {
                cell.setCellValue(n.doubleValue());
            }
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof java.util.Date d) {
            cell.setCellValue(d);
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }
}