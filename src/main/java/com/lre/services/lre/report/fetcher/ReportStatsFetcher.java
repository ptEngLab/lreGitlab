package com.lre.services.lre.report.fetcher;

import com.lre.common.exceptions.ReportFetchException;
import com.lre.db.SQLiteConnectionManager;
import com.lre.db.SqlQueries;
import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreRunInfo;
import com.lre.model.report.LreTxnStats;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class ReportStatsFetcher {

    // Generic method to fetch data based on SQL query and mapper
    public static <T> List<T> fetch(Path dbPath, String sqlQuery, List<Object> params, ResultSetMapper<T> mapper) {
        if (dbPath == null) return List.of();  // Return empty list if dbPath is null

        List<T> result = new ArrayList<>();

        try (SQLiteConnectionManager manager = new SQLiteConnectionManager(dbPath)) {
            if (params == null) params = new ArrayList<>(); // Handle null params gracefully

            manager.executeQuery(sqlQuery, params, rs -> {
                while (rs.next()) {
                    result.add(mapper.map(rs));  // Map each row using the provided mapper
                }
            });
        } catch (ReportFetchException e) {
            throw e; // already wrapped
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error during fetch: {}", sqlQuery, e);
            throw new ReportFetchException("Unexpected error during fetch for query: " + sqlQuery, e);
        }

        return result;
    }

    public static LreRunInfo fetchRunInfo(Path dbPath) {
        List<LreRunInfo> runInfoList = ReportStatsFetcher.fetch(dbPath, SqlQueries.RUN_INFO_SQL, null, LreRunInfo::from);
        if (!runInfoList.isEmpty()) return runInfoList.get(0);
        throw new ReportFetchException("Run info not found for the given query.");
    }

    public static List<LreTxnStats> fetchTransactions(Path dbPath) {
        return fetch(dbPath, SqlQueries.TXN_SUMMARY_SQL, null, LreTxnStats::from);
    }

    public static List<LreErrorStats> fetchErrors(Path dbPath) {
        return fetch(dbPath, SqlQueries.ERROR_SUMMARY_SQL, null, LreErrorStats::from);
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;  // more specific exception
    }
}