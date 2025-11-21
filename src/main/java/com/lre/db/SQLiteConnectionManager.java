package com.lre.db;

import com.lre.excel.ExcelExporter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

@Slf4j
public record SQLiteConnectionManager(String dbPath) {

    public void exportToExcelV2(String sql, String excelFilePath, String sheetName) {
        exportToExcelV2(sql, excelFilePath, sheetName, null);
    }

    public void exportToExcelV2(String sql, String excelFilePath, String sheetName, List<Object> parameters) {
        try (Connection conn = getOptimizedConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, parameters);
             ResultSet rs = stmt.executeQuery()) {

            ExcelExporter
                    .fromResultSet(rs)
                    .sheet(sheetName)
                    .writeTo(excelFilePath);

        } catch (Exception e) {
            log.error("Excel export failed: {}", e.getMessage(), e);
            throw new RuntimeException("Excel export failed", e);
        }
    }

    public void exportToExcelV2WithMerging(String sql, String excelFilePath, String sheetName, String mergeColumn) {
        exportToExcelV2WithMerging(sql, excelFilePath, sheetName, mergeColumn, null);
    }

    public void exportToExcelV2WithMerging(String sql, String excelFilePath, String sheetName,
                                           String mergeColumn, List<Object> parameters) {
        try (Connection conn = getOptimizedConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, parameters);
             ResultSet rs = stmt.executeQuery()) {

            ExcelExporter
                    .fromResultSet(rs)
                    .sheet(sheetName)
                    .mergeOn(mergeColumn)
                    .writeTo(excelFilePath);

        } catch (Exception e) {
            log.error("Excel merging export failed: {}", e.getMessage(), e);
            throw new RuntimeException("Excel merging export failed", e);
        }
    }


    private PreparedStatement prepareStatement(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
        }
        return stmt;
    }

    private Connection getOptimizedConnection() throws SQLException, FileNotFoundException {

        verifyDatabaseExists(dbPath);

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA journal_mode = MEMORY");
            s.execute("PRAGMA synchronous = OFF");
            s.execute("PRAGMA cache_size = 10000");
            s.execute("PRAGMA temp_store = MEMORY");
            s.execute("PRAGMA mmap_size = 268435456");
            s.execute("PRAGMA busy_timeout = 30000");
        } catch (SQLException e) {
            conn.close();
            throw e;
        }

        conn.setAutoCommit(true);
        return conn;
    }


    private void verifyDatabaseExists(String dbPath) throws FileNotFoundException {
        if (!Files.exists(Paths.get(dbPath))) {
            log.error("Database file not found: {}", dbPath);
            throw new FileNotFoundException("Database file not found: " + dbPath);
        }
    }


}