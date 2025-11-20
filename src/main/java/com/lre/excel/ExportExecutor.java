package com.lre.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

@Slf4j
public record ExportExecutor(ResultSet rs, String sheetName, String mergeColumn, String filePath) {

    void execute() {
        try {
            createDirectories(filePath);

            try (var wb = new XSSFWorkbook()) {
                var sheet = wb.createSheet(sheetName);
                ResultSetMetaData md = rs.getMetaData();

                var styler = new SheetStyler(wb, md);
                var writer = new SheetWriter(rs, sheet, md, styler, mergeColumn);

                writer.writeData();

                try (var fos = new FileOutputStream(filePath)) {
                    wb.write(fos);
                }
            }

            log.info("Excel export completed: {}", filePath);

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