package com.lre.services.lre.report.fetcher;

import com.lre.db.SQLiteConnectionManager;
import com.lre.model.transactions.LreTxnStats;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TransactionStatsFetcher {

    /**
     * Fetch transaction stats from the given database using the provided SQL query.
     *
     * @param dbPath   Path to the SQLite database
     * @param sqlQuery SQL query to execute
     * @param params   Optional query parameters (nullable)
     * @return List of LreTxnStats
     */
    public static List<LreTxnStats> fetch(Path dbPath, String sqlQuery, List<Object> params) {

        List<LreTxnStats> result = new ArrayList<>();

        try (SQLiteConnectionManager manager = new SQLiteConnectionManager(dbPath)) {
            manager.executeQuery(sqlQuery, params, rs -> {
                while (rs.next()) {
                    result.add(map(rs));
                }
            });
        }

        return result;
    }

    /**
     * Default fetch using TXN_SUMMARY_SQL
     */
    public static List<LreTxnStats> fetch(Path dbPath) {
        return fetch(dbPath, com.lre.db.SqlQueries.TXN_SUMMARY_SQL, null);
    }

    private static LreTxnStats map(ResultSet rs) {
        try {
            return new LreTxnStats(
                    rs.getString("Script_Name"),
                    rs.getString("Transaction_Name"),
                    rs.getInt("Transaction_Count"),
                    rs.getDouble("Minimum"),
                    rs.getDouble("Maximum"),
                    rs.getDouble("Average"),
                    rs.getDouble("Std_Deviation"),
                    rs.getInt("Pass"),
                    rs.getInt("Fail"),
                    rs.getDouble("p50"),
                    rs.getDouble("p90"),
                    rs.getDouble("p95"),
                    rs.getDouble("p99")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read transaction stats", e);
        }
    }
}
