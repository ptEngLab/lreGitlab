package com.lre.db;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

@Slf4j
public record SQLiteConnectionManager(Path dbPath) {

    private static final List<String> PRAGMA_LIST = List.of(
            "PRAGMA journal_mode = MEMORY",
            "PRAGMA synchronous = OFF",
            "PRAGMA cache_size = 10000",
            "PRAGMA temp_store = MEMORY",
            "PRAGMA mmap_size = 268435456",
            "PRAGMA busy_timeout = 30000"
    );

    public SQLiteConnectionManager(Path dbPath) {
        this.dbPath = dbPath;
        verifyDatabaseExists();
    }

    /**
     * Executes a SELECT query with optional parameters and passes the ResultSet to the consumer.
     */
    public void executeQuery(String sql, List<Object> parameters, ResultSetConsumer consumer) {
        try (Connection conn = createNewConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParameters(stmt, parameters);

            try (ResultSet rs = stmt.executeQuery()) {
                consumer.accept(rs);
            }

        } catch (Exception e) {
            log.error("Query execution failed: {}", sql, e);
            throw new SQLiteQueryException("Failed to execute query", e);
        }
    }

    /**
     * Creates a new SQLite connection with performance optimizations.
     */
    private Connection createNewConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath.toUri();
        Connection conn = DriverManager.getConnection(url);

        try (Statement stmt = conn.createStatement()) {
            for (String pragma : PRAGMA_LIST) {
                stmt.execute(pragma);
            }
        }

        conn.setAutoCommit(true);
        return conn;
    }

    private void bindParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters != null && !parameters.isEmpty()) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
        }
    }

    private void verifyDatabaseExists() {
        if (!Files.exists(dbPath)) {
            throw new SQLiteQueryException("Database file not found: " + dbPath.toAbsolutePath());
        }
    }

    @FunctionalInterface
    public interface ResultSetConsumer {
        void accept(ResultSet rs) throws Exception;
    }

    public static class SQLiteQueryException extends RuntimeException {
        public SQLiteQueryException(String message) {
            super(message);
        }

        public SQLiteQueryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
