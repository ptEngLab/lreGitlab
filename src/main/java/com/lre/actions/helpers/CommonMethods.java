package com.lre.actions.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
public class CommonMethods {

    @SuppressWarnings("unchecked")
    public static <T> T convertToType(String value, T defaultValue) {
        if (StringUtils.isBlank(value)) return defaultValue;
        String trimmedValue = value.trim();
        if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(trimmedValue);
        } else if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(trimmedValue);
        } else if (defaultValue instanceof Long) {
            return (T) Long.valueOf(trimmedValue);
        } else if (defaultValue instanceof Double) {
            return (T) Double.valueOf(trimmedValue);
        } else if (defaultValue instanceof String) {
            return (T) trimmedValue;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + defaultValue.getClass().getName());
        }
    }

    public static String createLogFileName() {
        LocalDate today = LocalDate.now();
        String logFileName = String.format(LRE_LOG_FILE, today.format(DateTimeFormatter.ISO_DATE));
        Path logDirPath = Path.of(DEFAULT_OUTPUT_DIR, LRE_ARTIFACTS_DIR);
        try {
            Files.createDirectories(logDirPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for log file: " + logDirPath, e);
        }
        return logDirPath.resolve(logFileName).toAbsolutePath().toString();
    }

    public static void writeRunIdToFile(int runId){
        try (FileWriter writer = new FileWriter(LRE_RUN_ID_FILE)){
            writer.write(String.valueOf(runId));
            log.info("RunId {} has been updated for cleanup", runId);
        } catch (IOException e) {
            log.error("Failed to write run id: {}", e.getMessage());
        }
    }

}
