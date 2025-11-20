package com.lre.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class MergeCollector {
    private final int columnIndex;
    private final List<CellRangeAddress> regions = new ArrayList<>();
    private String currentValue = null;
    private int groupStartRow = -1;

    MergeCollector(ResultSetMetaData md, String columnName) throws SQLException {
        this.columnIndex = findColumn(md, columnName);
    }

    void track(ResultSet rs, int rowNum) throws SQLException {
        String value = rs.getString(columnIndex + 1);

        if (currentValue == null) {
            currentValue = value;
            groupStartRow = rowNum;
            return;
        }

        if (!Objects.equals(currentValue, value)) {
            if (rowNum - 1 > groupStartRow) {
                regions.add(new CellRangeAddress(groupStartRow, rowNum - 1, columnIndex, columnIndex));
            }
            currentValue = value;
            groupStartRow = rowNum;
        }
    }

    void apply(Sheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        if (groupStartRow >= 0 && lastRowNum > groupStartRow) {
            regions.add(new CellRangeAddress(groupStartRow, lastRowNum, columnIndex, columnIndex));
        }

        for (CellRangeAddress region : regions) {
            sheet.addMergedRegion(region);
        }

        if (!regions.isEmpty()) {
            log.debug("Applied {} merge regions for column index {}", regions.size(), columnIndex);
        }
    }

    private static int findColumn(ResultSetMetaData md, String name) throws SQLException {
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (md.getColumnName(i).equalsIgnoreCase(name)) {
                return i - 1;
            }
        }

        List<String> availableColumns = new ArrayList<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            availableColumns.add(md.getColumnName(i));
        }
        throw new IllegalArgumentException(
                "Merge column '" + name + "' not found. Available columns: " + availableColumns);
    }
}