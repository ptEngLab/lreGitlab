package com.lre.services.lre.report.fetcher;

import com.lre.db.SQLiteConnectionManager;
import com.lre.db.SqlQueries;
import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreRunInfo;
import com.lre.model.report.LreTxnStats;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReportStatsFetcher {

    private ReportStatsFetcher() {
    }

    // Generic method to fetch data based on SQL query and mapper
    public static <T> List<T> fetch(Path dbPath, String sqlQuery, List<Object> params, ResultSetMapper<T> mapper) {
        if (dbPath == null) return List.of();  // Return empty list if dbPath is null

        List<T> result = new ArrayList<>();

        try (SQLiteConnectionManager manager = new SQLiteConnectionManager(dbPath)) {
            // Ensure parameters are not null (optional, for better safety)
            if (params == null) params = new ArrayList<>(); // Handle null params gracefully


            manager.executeQuery(sqlQuery, params, rs -> {
                while (rs.next()) {
                    result.add(mapper.map(rs));  // Map each row using the provided mapper
                }
            });
        } catch (Exception e) {
            // Log the error with the SQL query for easier debugging
            throw new RuntimeException("Failed to fetch data from DB with query: " + sqlQuery, e);
        }

        return result;
    }

    /**
     * Fetches the run information for a given database path.
     * <p>
     * This method queries the database for the run information, which is expected to return exactly one row.
     * If the query returns no results, it throws a {@link RuntimeException}.
     * </p>
     *
     * @param dbPath the path to the database containing the run information.
     * @return the {@link LreRunInfo} object representing the run information.
     * @throws RuntimeException if no run information is found in the database for the given path.
     */
    public static LreRunInfo fetchRunInfo(Path dbPath) {
        List<LreRunInfo> runInfoList = ReportStatsFetcher.fetch(dbPath, SqlQueries.RUN_INFO_SQL, null, LreRunInfo::from);
        if (!runInfoList.isEmpty()) return runInfoList.get(0);
        throw new RuntimeException("Run info not found for the given query.");
    }


    // Fetch Transaction Stats
    public static List<LreTxnStats> fetchTransactions(Path dbPath) {
        return fetch(dbPath, SqlQueries.TXN_SUMMARY_SQL, null, LreTxnStats::from);
    }

    // Fetch Error Stats
    public static List<LreErrorStats> fetchErrors(Path dbPath) {
        return fetch(dbPath, SqlQueries.ERROR_SUMMARY_SQL, null, LreErrorStats::from);
    }

    // Functional interface to map ResultSet to Java objects
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws Exception;  // Exception handling in mapper
    }
}
