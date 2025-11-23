package com.lre.excel;

import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.Map;

public class ExcelStyleFactory {

    private final Workbook wb;
    private final DataFormat fmt;
    private final Map<String, CellStyle> cache = new HashMap<>();

    public ExcelStyleFactory(Workbook wb) {
        this.wb = wb;
        this.fmt = wb.createDataFormat();
        init();
    }

    private void init() {
        getHeaderStyle();
        getDataStyle();
        getPlainDataStyle();
    }

    public CellStyle getHeaderStyle() {
        return cache.computeIfAbsent("header", k -> {
            CellStyle s = wb.createCellStyle();
            s.setAlignment(HorizontalAlignment.CENTER);
            s.setVerticalAlignment(VerticalAlignment.CENTER);

            Font f = wb.createFont();
            f.setBold(true);
            f.setFontHeightInPoints((short) 12);
            f.setColor(IndexedColors.WHITE.getIndex());
            s.setFont(f);

            s.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            setBorders(s);
            return s;
        });
    }

    public CellStyle getDataStyle() {
        return cache.computeIfAbsent("data", k -> {
            CellStyle s = wb.createCellStyle();
            s.setAlignment(HorizontalAlignment.LEFT);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(s);
            return s;
        });
    }

    /** Plain data style without borders */
    public CellStyle getPlainDataStyle() {
        return cache.computeIfAbsent("plainData", k -> {
            CellStyle s = wb.createCellStyle();
            s.setAlignment(HorizontalAlignment.LEFT);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            return s;
        });
    }

    public CellStyle getWrappedStyle() {
        return cache.computeIfAbsent("wrapped", k -> {
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(getDataStyle());
            s.setWrapText(true);
            return s;
        });
    }

    public CellStyle getDateStyle() {
        return cache.computeIfAbsent("date", k -> {
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(getDataStyle());
            s.setDataFormat(fmt.getFormat("dd-mmm-yyyy"));
            return s;
        });
    }

    public CellStyle getDateTimeStyle() {
        return cache.computeIfAbsent("datetime", k -> {
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(getDataStyle());
            s.setDataFormat(fmt.getFormat("dd-mmm-yyyy HH:mm:ss"));
            return s;
        });
    }

    public CellStyle getTimeStyle() {
        return cache.computeIfAbsent("time", k -> {
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(getDataStyle());
            s.setDataFormat(fmt.getFormat("HH:mm:ss"));
            return s;
        });
    }

    public CellStyle getNumericStyle() {
        return cache.computeIfAbsent("number", k -> {
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(getDataStyle());
            s.setDataFormat(fmt.getFormat("0.00"));
            return s;
        });
    }

    public CellStyle getIntegerStyle() {
        return cache.computeIfAbsent("int", k -> {
            CellStyle s = wb.createCellStyle();
            s.cloneStyleFrom(getDataStyle());
            s.setDataFormat(fmt.getFormat("0"));
            return s;
        });
    }

    private void setBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }
}
