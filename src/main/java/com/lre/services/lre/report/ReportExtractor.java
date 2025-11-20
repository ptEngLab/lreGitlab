package com.lre.services.lre.report;

import com.lre.db.SQLiteConnectionManager;
import com.lre.db.SqlQueries;
import com.lre.excel.ReportFileManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public record ReportExtractor(String baseDbPath, int runId) {

    public void extractRunReports() throws IOException {
        Path excelFilePath = ReportFileManager.getExcelFilePath(runId);
        try {
            ReportFileManager.deleteFileIfExists(excelFilePath);
            ReportFileManager.createDirectoriesIfNotExist(excelFilePath.getParent());

            exportSummaryReport(excelFilePath.toString());
            exportErrorReport(excelFilePath.toString());

            log.info("Successfully exported results to: {}", excelFilePath);

        } catch (Exception e) {
            log.error("Failed to extract run reports", e);
            throw new IOException("Database export failed", e);
        }
    }

    /**
     * Exports the summary report from the results.db database.
     */
    private void exportSummaryReport(String excelFilePath) throws IOException {
        String dbPath = getResultsDbPath();
        verifyDatabaseExists(dbPath);
        SQLiteConnectionManager dbManager = new SQLiteConnectionManager(dbPath);

        dbManager.exportToExcelV2WithMerging(SqlQueries.TXN_SUMMARY_SQL, excelFilePath, "TransactionSummary", "ScriptName");

        List<Object> parameters = List.of("Alternative", "201-03-05 00:00:00", "2013-03-05 00:00:00");
        dbManager.exportToExcelV2WithMerging(SqlQueries.ERROR_SUMMARY_SQL, excelFilePath, "summaryResults", "ScriptName",  parameters);
    }

    /**
     * Exports the error report from the sqlitedb.db database.
     */
    private void exportErrorReport(String excelFilePath) throws IOException {
        String dbPath = getErrorsDbPath();
        verifyDatabaseExists(dbPath);
        SQLiteConnectionManager dbManager = new SQLiteConnectionManager(dbPath);

        // Export error report without merging
        dbManager.exportToExcelV2(SqlQueries.TXN_SUMMARY_SQL, excelFilePath, "errorReports");
    }

    /**
     * Verifies that the database file exists.
     */
    private void verifyDatabaseExists(String dbPath) throws IOException {
        if (!Files.exists(Paths.get(dbPath))) {
            log.error("Database file not found: {}", dbPath);
            throw new IOException("Database file not found: " + dbPath);
        }
    }


    private String getResultsDbPath() {
        return baseDbPath+ "/Results_" + runId + ".db";
    }

    private String getErrorsDbPath() {
        return baseDbPath + "/sqlitedb.db";
    }

}

