package com.lre.db;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

@Slf4j
public record SQLiteConnectionManager(String dbPath) {

    /**
     * Executes a SQL query with optional parameters and passes the ResultSet to the consumer.
     * Resources (Connection, Statement, ResultSet) are automatically closed after the consumer completes.
     */
    public void executeQuery(String sql, List<Object> parameters, ResultSetConsumer consumer) {
        verifyDatabaseExists();

        try (Connection conn = createOptimizedConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind parameters if any
            if (parameters != null) {
                for (int i = 0; i < parameters.size(); i++) {
                    stmt.setObject(i + 1, parameters.get(i));
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                consumer.accept(rs);
            }

        } catch (Exception e) {
            log.error("Query execution failed: {}", sql, e);
            throw new RuntimeException("SQLite query execution failed", e);
        }
    }

    /**
     * Creates a SQLite connection with performance optimizations.
     */
    private Connection createOptimizedConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode = MEMORY");
            stmt.execute("PRAGMA synchronous = OFF");
            stmt.execute("PRAGMA cache_size = 10000");
            stmt.execute("PRAGMA temp_store = MEMORY");
            stmt.execute("PRAGMA mmap_size = 268435456");
            stmt.execute("PRAGMA busy_timeout = 30000");
        } catch (SQLException e) {
            conn.close();
            throw e;
        }

        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * Verifies that the database file exists.
     */
    private void verifyDatabaseExists() {
        if (!Files.exists(Path.of(dbPath))) {
            throw new RuntimeException("Database file not found: " + dbPath);
        }
    }

    /**
     * Functional interface for consuming a ResultSet.
     */
    @FunctionalInterface
    public interface ResultSetConsumer {
        void accept(ResultSet rs) throws Exception;
    }
}
