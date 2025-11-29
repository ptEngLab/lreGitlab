package com.lre.services.lre.report.fetcher;

import com.lre.db.SQLiteConnectionManager;
import com.lre.db.SqlQueries;
import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreTxnStats;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReportStatsFetcher {


    private ReportStatsFetcher() {
    }

    public static <T> List<T> fetch(Path dbPath, String sqlQuery, List<Object> params, ResultSetMapper<T> mapper) {
        if (dbPath == null) return List.of();

        List<T> result = new ArrayList<>();

        try (SQLiteConnectionManager manager = new SQLiteConnectionManager(dbPath)) {
            manager.executeQuery(sqlQuery, params, rs -> {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from DB: " + dbPath, e);
        }

        return result;
    }

    public static List<LreTxnStats> fetchTransactions(Path dbPath) {
        return fetch(dbPath, SqlQueries.TXN_SUMMARY_SQL, null, LreTxnStats::from);
    }

    public static List<LreErrorStats> fetchErrors(Path dbPath) {
        return fetch(dbPath, SqlQueries.ERROR_SUMMARY_SQL, null, LreErrorStats::from);
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws Exception;
    }
}

