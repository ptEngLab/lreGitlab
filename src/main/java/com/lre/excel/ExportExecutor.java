package com.lre.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

@Slf4j
public record ExportExecutor(ResultSet rs, String sheetName, String mergeColumn, String filePath) {

    void execute() {
        try {
            // Ensure directories exist
            createDirectories(filePath);

            Workbook wb;

            // Check if file exists, if yes, open it; if not, create a new workbook
            File file = new File(filePath);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    wb = new XSSFWorkbook(fis); // Open existing workbook
                }
            } else {
                wb = new XSSFWorkbook(); // Create a new workbook
            }

            // Create a new sheet in the workbook
            Sheet sheet = wb.createSheet(sheetName);

            ResultSetMetaData md = rs.getMetaData();

            // Use SheetStyler to handle styling and SheetWriter to write data
            var styler = new SheetStyler(wb, md);
            var writer = new SheetWriter(rs, sheet, md, styler, mergeColumn);

            // Write data to the sheet
            writer.writeData();

            // Save the updated workbook to the file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }

            log.debug("Excel export completed: {}", filePath);

        } catch (Exception e) {
            log.error("Export failed", e);
            throw new RuntimeException(e);
        }
    }

    private static void createDirectories(String path) {
        try {
            Path p = Paths.get(path).getParent();
            if (p != null && !Files.exists(p)) {
                Files.createDirectories(p);
                log.debug("Created directories: {}", p);
            }
        } catch (Exception e) {
            log.warn("Failed to create directories for path: {}", path, e);
        }
    }
}
