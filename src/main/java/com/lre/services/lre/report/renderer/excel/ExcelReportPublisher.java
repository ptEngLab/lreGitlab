package com.lre.services.lre.report.renderer.excel;

import com.lre.excel.ExcelDashboardWriter;
import com.lre.excel.ExcelReportEngine;
import com.lre.excel.ExcelReportFileManager;
import com.lre.services.lre.report.fetcher.ReportDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.*;

@Slf4j
public class ExcelReportPublisher {

    private final Path baseDbPath;
    private final int runId;
    private final Path excelFilePath;

    public ExcelReportPublisher(Path baseDbPath, int runId) {
        this.baseDbPath = baseDbPath.toAbsolutePath().normalize();
        this.runId = runId;
        this.excelFilePath = ExcelReportFileManager.getExcelFilePath(runId);
    }

    public void export(List<ExcelDashboardWriter.Section> dashboardSections) throws IOException {
        ExcelReportEngine engine = new ExcelReportEngine();
        Workbook workbook = engine.getWorkbook();

        if (dashboardSections != null && !dashboardSections.isEmpty()) {
            engine.getDashboard().writeDashboardSheet(TEST_SUMMARY_SHEET_NAME, dashboardSections);
        }

        ReportDataService.ReportData reportData =
                ReportDataService.fetchReportData(baseDbPath, runId);

        engine.getSheetWriter().writeModelSheet(
                TRANSACTION_SUMMARY_SHEET_NAME,
                reportData.transactions(),
                TXN_SUMMARY_MERGE_COLUMN_NAME
        );
        engine.getSheetWriter().writeModelSheet(
                ERROR_SUMMARY_SHEET_NAME,
                reportData.errors(),
                null
        );

        ExcelReportFileManager.createDirectoriesIfNotExist(excelFilePath.getParent());
        ExcelReportFileManager.deleteFileIfExists(excelFilePath);
        ExcelReportFileManager.saveWorkbook(workbook, excelFilePath);

        log.info("Excel report exported successfully: {}", excelFilePath);
    }

}
