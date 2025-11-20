package com.lre.excel;

import org.apache.poi.ss.usermodel.*;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class SheetStyler {
    private final Workbook wb;
    private final ResultSetMetaData md;
    private final Map<String, CellStyle> columnStyles;
    private final CellStyle headerStyle;

    SheetStyler(Workbook wb, ResultSetMetaData md) throws Exception {
        this.wb = wb;
        this.md = md;
        this.headerStyle = createHeaderStyle(wb);
        this.columnStyles = buildColumnStyles();
    }

    CellStyle getHeaderStyle() {
        return headerStyle;
    }

    CellStyle getColumnStyle(String columnName) {
        return columnStyles.get(columnName.toLowerCase());
    }

    private Map<String, CellStyle> buildColumnStyles() throws Exception {
        Map<String, CellStyle> map = new HashMap<>();
        var helper = wb.getCreationHelper();
        var fmt = helper.createDataFormat();

        for (int i = 1; i <= md.getColumnCount(); i++) {
            String col = md.getColumnName(i).toLowerCase();
            int sqlType = md.getColumnType(i);

            CellStyle style = createBaseStyle(wb);
            style.setAlignment(HorizontalAlignment.LEFT);

            switch (sqlType) {
                case Types.INTEGER, Types.BIGINT -> style.setDataFormat(fmt.getFormat("0"));
                case Types.FLOAT, Types.DOUBLE, Types.DECIMAL, Types.REAL, Types.NUMERIC -> 
                    style.setDataFormat(fmt.getFormat("#,##0.00"));
                case Types.DATE -> style.setDataFormat(fmt.getFormat("yyyy-mm-dd"));
                case Types.TIMESTAMP -> style.setDataFormat(fmt.getFormat("yyyy-mm-dd hh:mm:ss"));
                default -> style.setDataFormat(fmt.getFormat("@"));
            }

            map.put(col, style);
        }
        return map;
    }

    private static CellStyle createBaseStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = createBaseStyle(wb);
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }
}