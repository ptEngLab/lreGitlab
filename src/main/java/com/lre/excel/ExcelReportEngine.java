package com.lre.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Getter
public class ExcelReportEngine {

    private final Workbook workbook;
    private final ExcelStyleFactory styles;
    private final ExcelValueWriter valueWriter;
    private final ExcelSheetWriter sheetWriter;
    private final ExcelDashboardWriter dashboard;

    public ExcelReportEngine() {
        this.workbook = new XSSFWorkbook();
        this.styles = new ExcelStyleFactory(workbook);
        this.valueWriter = new ExcelValueWriter(styles);
        this.sheetWriter = new ExcelSheetWriter(workbook, styles, valueWriter);
        this.dashboard = new ExcelDashboardWriter(workbook, styles, valueWriter);
    }
}
