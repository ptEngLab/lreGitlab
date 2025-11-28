package com.lre.services.lre.report.renderer.excel;

import com.lre.common.exceptions.LreException;
import com.lre.excel.ExcelDashboardWriter;
import com.lre.excel.ExcelReportEngine;
import com.lre.excel.ExcelReportFileManager;
import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreTxnStats;
import com.lre.services.lre.report.fetcher.ReportDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.nio.file.Path;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.*;

/**
 * Publishes Excel reports based on LRE run data.
 */
@Slf4j
public class ExcelReportPublisher {

    private final Path excelFilePath;
    private final ExcelReportEngine engine;

    public ExcelReportPublisher(Path excelFilePath) {
        this.excelFilePath = excelFilePath;
        this.engine = new ExcelReportEngine();
    }

    public ExcelReportPublisher(int runId) {
        this(ExcelReportFileManager.getExcelFilePath(runId));
    }

    /**
     * Export Excel report using pre-fetched ReportData.
     *
     * @param dashboardSections sections for dashboard sheet
     * @param reportData        pre-fetched report data
     */
    public void export(List<ExcelDashboardWriter.Section> dashboardSections,
                       ReportDataService.ReportData reportData) {

        try {
            Workbook workbook = engine.getWorkbook();

            // Dashboard sheet
            if (dashboardSections != null && !dashboardSections.isEmpty()) {
                engine.getDashboard().writeDashboardSheet(TEST_SUMMARY_SHEET_NAME, dashboardSections);
            }

            // Transaction & Error sheets
            List<LreTxnStats> txnStatsAll = reportData.transactions();
            List<LreErrorStats> errorStatsAll = reportData.errors();

            engine.getSheetWriter().writeModelSheet(TRANSACTION_SUMMARY_SHEET_NAME, txnStatsAll, TXN_SUMMARY_MERGE_COLUMN_NAME);
            engine.getSheetWriter().writeModelSheet(ERROR_SUMMARY_SHEET_NAME, errorStatsAll, null);

            // Save workbook
            ExcelReportFileManager.createDirectoriesIfNotExist(excelFilePath.getParent());
            ExcelReportFileManager.deleteFileIfExists(excelFilePath);
            ExcelReportFileManager.saveWorkbook(workbook, excelFilePath);

            log.info("Excel report exported successfully: {}", excelFilePath);

        } catch (Exception e) {
            throw new LreException("Failed to export Excel report to " + excelFilePath, e);
        }
    }
}
