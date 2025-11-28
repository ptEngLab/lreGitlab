package com.lre.db;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

@Slf4j
public class SQLiteConnectionManager implements AutoCloseable {

    private static final List<String> PRAGMA_LIST = List.of(
            "PRAGMA journal_mode = MEMORY",
            "PRAGMA synchronous = OFF",
            "PRAGMA cache_size = 10000",
            "PRAGMA temp_store = MEMORY",
            "PRAGMA mmap_size = 268435456",
            "PRAGMA busy_timeout = 30000"
    );

    private final Path dbPath;
    private final Connection connection;

    public SQLiteConnectionManager(Path dbPath) {
        this.dbPath = dbPath;
        verifyDatabaseExists();
        this.connection = createConnectionWithPragmaList();
    }

    /**
     * Executes a SELECT query and processes the ResultSet.
     */
    public void executeQuery(String sql, List<Object> parameters, ResultSetConsumer consumer) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            bindParameters(stmt, parameters);

            try (ResultSet rs = stmt.executeQuery()) {
                consumer.accept(rs);
            }

        } catch (Exception e) {
            log.error("Query execution failed: {} | params: {}", sql, parameters, e);
            throw new SQLiteQueryException("Failed to execute query: " + sql, e);
        }
    }

    private Connection createConnectionWithPragmaList() {
        try {
            String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
            Connection conn = DriverManager.getConnection(url);

            try (Statement stmt = conn.createStatement()) {
                for (String pragma : PRAGMA_LIST) {
                    stmt.execute(pragma);
                }
            }

            conn.setAutoCommit(true);
            return conn;

        } catch (SQLException e) {
            throw new SQLiteQueryException("Failed to create SQLite connection", e);
        }
    }

    private void bindParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters == null) return;

        for (int i = 0; i < parameters.size(); i++) {
            stmt.setObject(i + 1, parameters.get(i));
        }
    }

    private void verifyDatabaseExists() {
        if (!Files.exists(dbPath)) {
            throw new SQLiteQueryException("Database not found: " + dbPath.toAbsolutePath());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.debug("SQLite connection closed: {}", dbPath);
            }
        } catch (SQLException e) {
            log.warn("Failed to close SQLite connection", e);
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
