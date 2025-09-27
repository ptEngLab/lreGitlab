package com.lre.actions.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
public class CommonMethods {

    @FunctionalInterface
    public interface ExceptionSupplier {
        RuntimeException create(String message);
    }

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

    public static void writeRunIdToFile(int runId) {
        try (FileWriter writer = new FileWriter(LRE_RUN_ID_FILE)) {
            writer.write(String.valueOf(runId));
            log.debug("RunId {} has been updated for cleanup", runId);
        } catch (IOException e) {
            log.error("Failed to write run id: {}", e.getMessage());
        }
    }

    public static String logTableDynamic(String[][] rows) {
        int columns = rows[0].length;
        int[] columnWidths = new int[columns];

        // Calculate max width per column
        for (int i = 0; i < columns; i++) {
            for (String[] row : rows) {
                if (i < row.length && row[i] != null) {
                    columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                }
            }
        }

        // Build border
        StringBuilder borderBuilder = new StringBuilder("+");
        for (int w : columnWidths) {
            borderBuilder.append("-".repeat(w + 2)).append("+");
        }
        String border = borderBuilder + "\n"; // Add newline after a border

        // Row format
        StringBuilder formatBuilder = new StringBuilder("|");
        for (int w : columnWidths) {
            formatBuilder.append(" %-").append(w).append("s |");
        }
        String rowFormat = formatBuilder + "\n"; // Add newline after row

        // Build table
        StringBuilder sb = new StringBuilder("\n");
        sb.append(border);

        for (String[] row : rows) {
            sb.append(String.format(rowFormat, (Object[]) row));
        }
        sb.append(border);

        return sb.toString();
    }

    public static String normalizePathWithSubject(String path) {
        if (StringUtils.isBlank(path)) return null;

        // Step 1: Normalize all slashes to backslashes
        String normalized = path.replace("/", "\\");

        // Step 2: Remove repeated backslashes (e.g., "folder\\\\sub" -> "folder\sub")
        normalized = normalized.replaceAll("\\\\+", "\\\\");

        // Step 3: Trim leading and trailing backslashes
        normalized = normalized.replaceAll("^\\\\+|\\\\+$", "");

        // Step 4: Ensure "Subject\" prefix (case-insensitive)
        if (!normalized.regionMatches(true, 0, "Subject\\", 0, "Subject\\".length())) {
            normalized = "Subject\\" + normalized;
        }

        return normalized;
    }

    public static <T extends Number> T parsePositive(
            String value,
            String name,
            Function<String, T> parser,
            ExceptionSupplier exceptionSupplier,
            String validExamples
    ) {
        try {
            T parsed = parser.apply(value);
            if (parsed.doubleValue() <= 0) {
                throw exceptionSupplier.create(name + " must be positive. Got: " + parsed + validExamples);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw exceptionSupplier.create("Invalid " + name + ": '" + value + "'. Must be a positive number." + validExamples);
        }
    }
}
