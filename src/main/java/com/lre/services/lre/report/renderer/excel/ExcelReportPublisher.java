package com.lre.services.lre.report.renderer.excel;

import com.lre.db.SQLiteConnectionManager;
import com.lre.excel.ExcelDashboardWriter;
import com.lre.excel.ExcelReportEngine;
import com.lre.excel.ExcelReportFileManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.*;
import static com.lre.db.SqlQueries.ERROR_SUMMARY_SQL;
import static com.lre.db.SqlQueries.TXN_SUMMARY_SQL;

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

        // Dashboard sheet
        if (dashboardSections != null && !dashboardSections.isEmpty()) {
            engine.getDashboard().writeDashboardSheet(TEST_SUMMARY_SHEET_NAME, dashboardSections);
        }

        // Query-based sheets
        try (SQLiteConnectionManager resultsManager = new SQLiteConnectionManager(resultsDb);
             SQLiteConnectionManager errorsManager = new SQLiteConnectionManager(errorsDb)) {

            log.info("Extracting transaction summary from DB");
            resultsManager.executeQuery(TXN_SUMMARY_SQL, null,
                    rs -> engine.getSheetWriter().writeResultSetSheet(
                            TRANSACTION_SUMMARY_SHEET_NAME, rs, TXN_SUMMARY_MERGE_COLUMN_NAME));

            log.info("Extracting error summary from DB");
            errorsManager.executeQuery(ERROR_SUMMARY_SQL, null,
                    rs -> engine.getSheetWriter().writeResultSetSheet(
                            ERROR_SUMMARY_SHEET_NAME, rs, null));
        }

        // Save workbook
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
