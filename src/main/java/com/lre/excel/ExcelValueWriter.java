package com.lre.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ExcelValueWriter(ExcelStyleFactory styles) {

    /**
     * Write a value into a cell with an explicit style.
     * If style is null, falls back to the default style logic.
     */
    public void write(Cell cell, Object value, CellStyle style) {
        if (style != null) {
            // caller wants to override style, just set value + style
            setCellValue(cell, value);
            cell.setCellStyle(style);
        } else {
            // delegate to default overload
            write(cell, value);
        }
    }

    /**
     * Write a value into a cell using default style logic.
     */
    public void write(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            cell.setCellStyle(styles.getDataStyle());
            return;
        }

        // --- Numbers: INT ---
        if (value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
            cell.setCellValue(((Number) value).longValue());
            cell.setCellStyle(styles.getIntegerStyle());
            return;
        }

        // --- Numbers: DECIMAL ---
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            cell.setCellValue(((Number) value).doubleValue());
            cell.setCellStyle(styles.getNumericStyle());
            return;
        }

        // --- Numeric Strings (SQLite often returns numbers as Strings) ---
        if (value instanceof String s && isNumeric(s)) {
            cell.setCellValue(Double.parseDouble(s));
            cell.setCellStyle(styles.getNumericStyle());
            return;
        }

        if (value instanceof Boolean b) {
            cell.setCellValue(b);
            cell.setCellStyle(styles.getDataStyle());
            return;
        }

        if (value instanceof LocalDateTime dt) {
            cell.setCellValue(dt);
            cell.setCellStyle(styles.getDateTimeStyle());
            return;
        }

        if (value instanceof LocalDate d) {
            cell.setCellValue(d);
            cell.setCellStyle(styles.getDateStyle());
            return;
        }

        if (value instanceof LocalTime t) {
            double excelTime = t.toSecondOfDay() / 86400.0;
            cell.setCellValue(excelTime);
            cell.setCellStyle(styles.getTimeStyle());
            return;
        }

        if (value instanceof Timestamp ts) {
            cell.setCellValue(ts.toLocalDateTime());
            cell.setCellStyle(styles.getDateTimeStyle());
            return;
        }

        if (value instanceof Date d) {
            cell.setCellValue(d.toLocalDate());
            cell.setCellStyle(styles.getDateStyle());
            return;
        }

        // Fallback: string
        cell.setCellValue(value.toString());
        cell.setCellStyle(styles.getDataStyle());
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof LocalDateTime dt) {
            cell.setCellValue(dt);
        } else if (value instanceof LocalDate d) {
            cell.setCellValue(d);
        } else if (value instanceof LocalTime t) {
            cell.setCellValue(t.toSecondOfDay() / 86400.0);
        } else if (value instanceof Timestamp ts) {
            cell.setCellValue(ts.toLocalDateTime());
        } else if (value instanceof Date d) {
            cell.setCellValue(d.toLocalDate());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
