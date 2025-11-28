package com.lre.model.report;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LreTxnStats {

    private String scriptName;
    private String transactionName;
    private int transactionCount;
    private double minimum;
    private double maximum;
    private double average;
    private double stdDeviation;
    private int pass;
    private int fail;
    private double p50;
    private double p90;
    private double p95;
    private double p99;

    // Centralized mapper
    public static LreTxnStats from(ResultSet rs) throws SQLException {
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
    }
}
