package com.lre.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelSheetAutoSizer {

    // Constant extra width in 1/256th units
    private static final int EXTRA_WIDTH = 2000;

    /**
     * Auto-size all columns in the sheet and add constant extra space.
     *
     * @param sheet the Excel sheet
     */
    public static void autoSizeAllColumns(Sheet sheet) {
        if (sheet.getPhysicalNumberOfRows() == 0) return;

        // Find the maximum column index across all rows
        int maxCol = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getLastCellNum() > maxCol) {
                maxCol = row.getLastCellNum();
            }
        }

        // Auto-size every column up to maxCol (including gaps)
        for (int col = 0; col < maxCol; col++) {
            sheet.autoSizeColumn(col);
            int newWidth = sheet.getColumnWidth(col) + EXTRA_WIDTH;
            sheet.setColumnWidth(col, Math.min(newWidth, 255 * 256)); // clamp to Excel max
        }
    }

}
