package com.lre.excel;

import java.sql.ResultSet;

public class ExcelExporterV2 {
    private final ResultSet rs;
    private String sheetName = "Results";
    private String mergeColumn = null;

    private ExcelExporterV2(ResultSet rs) {
        this.rs = rs;
    }

    public static ExcelExporterV2 fromResultSet(ResultSet rs) {
        return new ExcelExporterV2(rs);
    }

    public ExcelExporterV2 sheet(String name) {
        this.sheetName = name;
        return this;
    }

    public ExcelExporterV2 mergeOn(String column) {
        this.mergeColumn = column;
        return this;
    }

    public void writeTo(String filePath) {
        new ExportExecutor(rs, sheetName, mergeColumn, filePath).execute();
    }
}