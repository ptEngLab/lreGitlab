package com.lre.services.lre.report.renderer.excel;

import com.lre.excel.ExcelDashboardWriter;
import com.lre.excel.ExcelReportEngine;
import com.lre.excel.ExcelReportFileManager;
import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreTxnStats;
import com.lre.services.lre.report.fetcher.ReportStatsFetcher;
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
        Path resultsDb = getResultsDbPath();
        Path errorsDb = getErrorsDbPath();

        ExcelReportEngine engine = new ExcelReportEngine();
        Workbook workbook = engine.getWorkbook();

        // 1. Dashboard sheet
        if (dashboardSections != null && !dashboardSections.isEmpty()) {
            engine.getDashboard().writeDashboardSheet(TEST_SUMMARY_SHEET_NAME, dashboardSections);
        }

        // 2. Fetch data using ReportStatsFetcher
        log.info("Fetching transaction summary from DB");
        List<LreTxnStats> txnStatsAll = ReportStatsFetcher.fetchTransactions(resultsDb);

        log.info("Fetching error summary from DB");
        List<LreErrorStats> errorStatsAll = ReportStatsFetcher.fetchErrors(errorsDb);


        // 4. Write sheets
        engine.getSheetWriter().writeModelSheet(TRANSACTION_SUMMARY_SHEET_NAME, txnStatsAll, TXN_SUMMARY_MERGE_COLUMN_NAME );
        engine.getSheetWriter().writeModelSheet(ERROR_SUMMARY_SHEET_NAME, errorStatsAll, null);

        // 5. Save workbook
        ExcelReportFileManager.createDirectoriesIfNotExist(excelFilePath.getParent());
        ExcelReportFileManager.deleteFileIfExists(excelFilePath);
        ExcelReportFileManager.saveWorkbook(workbook, excelFilePath);

        log.info("Excel report exported successfully: {}", excelFilePath);
    }

    private Path getResultsDbPath() {
        return baseDbPath.resolve(String.format(RESULTS_DB_FORMAT, runId));
    }

    private Path getErrorsDbPath() {
        return baseDbPath.resolve(ERRORS_DB_FORMAT);
    }
}
