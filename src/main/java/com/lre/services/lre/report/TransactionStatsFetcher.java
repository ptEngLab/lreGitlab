package com.lre.services.lre.report;

import com.lre.db.SQLiteConnectionManager;
import com.lre.model.transactions.LreTxnStats;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static com.lre.db.SqlQueries.TXN_SUMMARY_SQL;

public class TransactionStatsFetcher {

    public static List<LreTxnStats> fetch(Path dbPath) {
        SQLiteConnectionManager manager = new SQLiteConnectionManager(dbPath);
        List<LreTxnStats> result = new ArrayList<>();

        manager.executeQuery(TXN_SUMMARY_SQL, null, rs -> {
            while (rs.next()) {
                result.add(map(rs));
            }
        });

        return result;
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
