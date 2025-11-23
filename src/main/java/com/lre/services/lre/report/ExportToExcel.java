package com.lre.services.lre.report;

import com.lre.excel.ExcelDashboardWriter;
import com.lre.excel.ExcelReportEngine;
import com.lre.excel.ExcelReportFileManager;
import com.lre.db.SQLiteConnectionManager;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.lre.db.SqlQueries.*;

public class ExportToExcel {

    private final String baseDbPath;
    private final int runId;
    private final Path excelFilePath;

    private static final String TRANSACTION_SUMMARY_SHEET_NAME = "TransactionSummary";
    private static final String STEADY_STATE_TRANSACTION_SUMMARY_SHEET_NAME = "SteadyStateTxns";
    private static final String ERROR_SUMMARY_SHEET_NAME = "ErrorSummary";
    private static final String TEST_SUMMARY_SHEET_NAME = "TestSummary";
    private static final String TXN_SUMMARY_MERGE_COLUMN_NAME = "Script_Name";

    public ExportToExcel(String baseDbPath, int runId) {
        this.baseDbPath = baseDbPath;
        this.runId = runId;
        this.excelFilePath = ExcelReportFileManager.getExcelFilePath(runId);
    }

    public void export(List<ExcelDashboardWriter.Section> dashboardSections) throws IOException {
        String resultsDb = getResultsDbPath();
        String errorsDb = getErrorsDbPath();

        ExcelReportEngine engine = new ExcelReportEngine();
        Workbook workbook = engine.getWorkbook();

        // Dashboard sheet
        if (dashboardSections != null && !dashboardSections.isEmpty()) {
            engine.getDashboard().writeDashboardSheet(TEST_SUMMARY_SHEET_NAME, dashboardSections);
        }

        // Query-based sheets
        SQLiteConnectionManager resultsManager = new SQLiteConnectionManager(resultsDb);
        SQLiteConnectionManager errorsManager = new SQLiteConnectionManager(errorsDb);

        resultsManager.executeQuery(TXN_SUMMARY_SQL, null,
                rs -> engine.getSheetWriter().writeResultSetSheet(
                        TRANSACTION_SUMMARY_SHEET_NAME, rs, TXN_SUMMARY_MERGE_COLUMN_NAME));

        resultsManager.executeQuery(TRANSACTIONS_BY_CUSTOMER_AND_DATE_SQL,
                List.of("Alternative", "2013-03-05 00:00:00", "2013-03-05 00:00:00"),
                rs -> engine.getSheetWriter().writeResultSetSheet(
                        STEADY_STATE_TRANSACTION_SUMMARY_SHEET_NAME, rs, TXN_SUMMARY_MERGE_COLUMN_NAME));

        errorsManager.executeQuery(ERROR_SUMMARY_SQL, null,
                rs -> engine.getSheetWriter().writeResultSetSheet(
                        ERROR_SUMMARY_SHEET_NAME, rs, null));

        // Save workbook once
        ExcelReportFileManager.createDirectoriesIfNotExist(excelFilePath.getParent());
        ExcelReportFileManager.deleteFileIfExists(excelFilePath);
        ExcelReportFileManager.saveWorkbook(workbook, excelFilePath);
    }

    private String getResultsDbPath() {
        return baseDbPath + "/Results_" + runId + ".db";
    }

    private String getErrorsDbPath() {
        return baseDbPath + "/sqlitedb.db";
    }
}
