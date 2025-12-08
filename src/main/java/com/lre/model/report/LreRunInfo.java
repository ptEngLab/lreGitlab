package com.lre.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LreRunInfo {

    private int resultId;
    private String scenarioName;
    private String resultName;
    private int timeZone;
    private long startTime;
    private long endTime;

    // Mapper method
    public static LreRunInfo from(ResultSet rs) throws SQLException {
        return new LreRunInfo(
                rs.getInt("resultId"),
                rs.getString("scenarioName"),
                rs.getString("resultName"),
                rs.getInt("timeZone"),
                rs.getLong("startTime"),
                rs.getLong("endTime")
        );
    }
}
