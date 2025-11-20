package com.lre.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

@Slf4j
public record SQLiteConnectionManager(String dbPath) {

    public void exportToExcel(String sql, String excelFilePath) {
        exportToExcel(sql, excelFilePath, null);
    }

    public void exportToExcel(String sql, String excelFilePath, List<Object> parameters) {
        try (Connection conn = getOptimizedConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, parameters);
             ResultSet rs = stmt.executeQuery()) {

            ExcelExporter.exportToExcel(rs, excelFilePath, "Results");

        } catch (SQLException e) {
            log.error("Database error during export to Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Database error during export to Excel", e);
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Set parameters if provided
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
        }

        return stmt;
    }

    private Connection getOptimizedConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        try (Statement setupStmt = conn.createStatement()) {
            setupStmt.execute("PRAGMA journal_mode = MEMORY");
            setupStmt.execute("PRAGMA synchronous = OFF");
            setupStmt.execute("PRAGMA cache_size = 10000");
            setupStmt.execute("PRAGMA temp_store = MEMORY");
            setupStmt.execute("PRAGMA mmap_size = 268435456"); // 256MB memory mapping
            setupStmt.execute("PRAGMA busy_timeout = 30000"); // 30 second timeout
        }

        conn.setAutoCommit(false);
        return conn;
    }
}
