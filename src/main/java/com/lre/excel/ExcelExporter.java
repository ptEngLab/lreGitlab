package com.lre.excel;

import java.sql.ResultSet;

public class ExcelExporter {
    private final ResultSet rs;
    private String sheetName = "Results";
    private String mergeColumn = null;

    private ExcelExporter(ResultSet rs) {
        this.rs = rs;
    }

    public static ExcelExporter fromResultSet(ResultSet rs) {
        return new ExcelExporter(rs);
    }

    public ExcelExporter sheet(String name) {
        this.sheetName = name;
        return this;
    }

    public ExcelExporter mergeOn(String column) {
        this.mergeColumn = column;
        return this;
    }

    public void writeTo(String filePath) {
        new ExportExecutor(rs, sheetName, mergeColumn, filePath).execute();
    }
}