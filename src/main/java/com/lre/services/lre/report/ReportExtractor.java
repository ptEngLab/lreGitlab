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
public record ReportExtractor(String dbPath, int runId) {

    /**
     * Extracts run reports from the database and exports them to Excel.
     */
    public void extractRunReports() throws IOException {
        // Ensure the database exists
        verifyDatabaseExists();

        // Initialize the SQLiteConnectionManager
        SQLiteConnectionManager dbManager = new SQLiteConnectionManager(dbPath);

        // Get the output Excel file path
        Path excelFilePath = ReportFileManager.getExcelFilePath(runId);

        try {
            // Ensure the Excel file is created fresh
            ReportFileManager.deleteFileIfExists(excelFilePath);
            ReportFileManager.createDirectoriesIfNotExist(excelFilePath.getParent());

            log.info("Starting database export from: {}", dbPath);
            log.debug("Output Excel file: {}", excelFilePath);

            dbManager.exportToExcelV2WithMerging(SqlQueries.TXN_SUMMARY_SQL, excelFilePath.toString(), "GenreName");
            dbManager.exportToExcelV2(SqlQueries.ERROR_SUMMARY_SQL, excelFilePath.toString(), "Errors");

            List<Object> parameters = List.of("Alternative", "2010-03-05 00:00:00", "2013-03-05 00:00:00");
            dbManager.exportToExcelV2(SqlQueries.TRANSACTIONS_BY_CUSTOMER_AND_DATE_SQL, excelFilePath.toString(), "Data", parameters);

            log.info("Successfully exported results to: {}", excelFilePath);

        } catch (Exception e) {
            log.error("Failed to extract run reports from database: {}", dbPath, e);
            throw new IOException("Database export failed", e);
        }
    }

    /**
     * Verifies that the database file exists.
     */
    private void verifyDatabaseExists() throws IOException {
        if (!Files.exists(Paths.get(dbPath))) {
            log.error("Database file not found: {}", dbPath);
            throw new IOException("Database file not found: " + dbPath);
        }
    }
}
