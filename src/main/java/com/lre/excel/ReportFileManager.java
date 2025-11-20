package com.lre.excel;

import com.lre.common.constants.ConfigConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.lre.common.constants.ConfigConstants.ARTIFACTS_DIR;

@Slf4j
public class ReportFileManager {

    /**
     * Deletes the existing Excel file if it exists.
     */
    public static void deleteFileIfExists(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Deleted existing file: {}", filePath);
        }
    }

    /**
     * Creates the directories for the given file path if they do not exist.
     */
    public static void createDirectoriesIfNotExist(Path directoryPath) throws IOException {
        if (directoryPath != null && !Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
            log.debug("Created directories: {}", directoryPath);
        }
    }

    /**
     * Creates and returns the path for the Excel file.
     */
    public static Path getExcelFilePath(int runId) {
        return Path.of(ConfigConstants.DEFAULT_OUTPUT_DIR, ARTIFACTS_DIR, "output", "results_" + runId + ".xlsx");
    }


}
