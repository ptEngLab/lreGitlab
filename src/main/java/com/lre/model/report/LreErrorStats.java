package com.lre.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LreErrorStats {

    private String scriptName;
    private String injectorName;
    private String errorCode;
    private String errorMessage;
    private Integer totalErrorCount;
    private Integer affectedVusers;


    public static LreErrorStats from(ResultSet rs) throws SQLException {
        return new LreErrorStats(
                rs.getString("Script_Name"),
                rs.getString("injectorName"),
                rs.getString("errorCode"),
                rs.getString("errorMessage"),
                rs.getInt("totalErrorCount"),
                rs.getInt("affectedVusers")
        );
    }
}
