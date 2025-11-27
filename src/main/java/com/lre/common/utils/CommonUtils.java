package com.lre.common.utils;

import com.lre.common.exceptions.LreException;
import com.lre.model.testplan.LreTestPlanCreationRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.lre.common.constants.ConfigConstants.*;

@Slf4j
public class CommonUtils {

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
        Path logDirPath = Path.of(DEFAULT_OUTPUT_DIR, ARTIFACTS_DIR);
        try {
            Files.createDirectories(logDirPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for log file: " + logDirPath, e);
        }
        return logDirPath.resolve(logFileName).toAbsolutePath().toString();
    }

    public static void writeRunIdToFile(int runId) {
        // <cwd>/artifacts/reports/lre_run_id.env
        Path reportDir = Paths.get(DEFAULT_OUTPUT_DIR, ARTIFACTS_DIR, "reports");
        Path filePath = reportDir.resolve(LRE_RUN_ID_FILE);

        try {
            // Ensure directory exists
            Files.createDirectories(reportDir);

            // Write atomically
            Path tempFile = filePath.resolveSibling(LRE_RUN_ID_FILE + ".tmp");

            String content = "LRE_RUN_ID=" + runId + System.lineSeparator();

            Files.writeString(
                    tempFile,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            Files.move(
                    tempFile,
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

            log.info("Run ID {} written to {}", runId, filePath.toAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to write run ID: {}", e.getMessage(), e);
        }
    }




    public static String logTable(String[][] data) {
        return logTable(null, data);
    }

    public static String logTable(String[] header, String[][] dataRows) {
        if ((header == null || header.length == 0) && (dataRows == null || dataRows.length == 0)) {
            return "";
        }

        int columns = header != null ? header.length : dataRows[0].length;
        int[] columnWidths = new int[columns];

        // Compute max width per column (including header)
        for (int i = 0; i < columns; i++) {
            if (header != null && i < header.length && header[i] != null) {
                columnWidths[i] = Math.max(columnWidths[i], header[i].length());
            }
            if (dataRows != null) {
                for (String[] row : dataRows) {
                    if (i < row.length && row[i] != null) {
                        columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                    }
                }
            }
        }

        // Build horizontal border
        StringBuilder borderBuilder = new StringBuilder("+");
        for (int w : columnWidths) borderBuilder.append("-".repeat(w + 2)).append("+");
        String border = borderBuilder + "\n";

        // Build row format
        StringBuilder formatBuilder = new StringBuilder("|");
        for (int w : columnWidths) formatBuilder.append(" %-").append(w).append("s |");
        String rowFormat = formatBuilder + "\n";

        StringBuilder sb = new StringBuilder("\n");
        sb.append(border);

        // Add header if exists
        if (header != null) {
            sb.append(String.format(rowFormat, (Object[]) header));
            sb.append(border); // separate header from data
        }

        // Add data rows
        if (dataRows != null) {
            for (String[] row : dataRows) {
                sb.append(String.format(rowFormat, (Object[]) row));
            }
        }

        sb.append(border);
        return sb.toString();
    }

    public static String normalizePathWithSubject(String path) {
        if (StringUtils.isBlank(path)) return "Subject";

        // Step 1: Normalize all slashes to backslashes
        String normalized = path.replace("/", "\\");

        // Step 2: Remove repeated backslashes (e.g., "folder\\\\sub" -> "folder\sub")
        normalized = normalized.replaceAll("\\\\+", "\\\\");

        // Step 3: Trim leading and trailing backslashes
        normalized = normalized.replaceAll("^\\\\+|\\\\+$", "");

        // Step 4: Ensure "Subject\" prefix (case-insensitive)
        if (!normalized.toLowerCase().startsWith("subject\\")) {
            normalized = "Subject\\" + normalized;
        }

        return normalized;
    }

    public static String replaceBackSlash(String input) {
        return input.replace("\\", "/");
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

    public static void removeRunIdFile() {
        try {
            File runIdFile = new File(LRE_RUN_ID_FILE);

            if (!runIdFile.exists()) {
                log.debug("Run ID file does not exist, no need to delete");
                return;
            }

            if (runIdFile.delete()) {
                log.debug("Successfully deleted temporary Run ID file: {}", LRE_RUN_ID_FILE);
            } else {
                log.warn("Failed to delete temporary Run ID file. Path: {}, Absolute Path: {}, Readable: {}, Writable: {}",
                        LRE_RUN_ID_FILE,
                        runIdFile.getAbsolutePath(),
                        runIdFile.canRead(),
                        runIdFile.canWrite());
            }

        } catch (SecurityException e) {
            log.error("Security exception while trying to delete Run ID file '{}': {}", LRE_RUN_ID_FILE, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while trying to delete Run ID file '{}' - {}", LRE_RUN_ID_FILE, e.getMessage());
        }
    }

    public static void createZipFile(Path sourceDir, Path zipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String entryName = sourceDir.relativize(dir).toString().replace(File.separator, "/") + "/";
                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String entryName = sourceDir.relativize(file).toString().replace(File.separator, "/");
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Error creating zip file {}: {}", zipFile, e.getMessage(), e);
        }
    }


    public static void unzip(File zipFile, File destDir) throws IOException {
        Path destPath = destDir.toPath();
        if (Files.notExists(destPath)) {
            Files.createDirectories(destPath);
        }

        try (FileSystem zipFs = FileSystems.newFileSystem(zipFile.toPath(), (ClassLoader) null)) {
            for (Path root : zipFs.getRootDirectories()) {
                try (var stream = Files.walk(root)) {
                    stream.forEach(source -> {
                        try {
                            Path relative = root.relativize(source);
                            if (relative.toString().isEmpty()) return;

                            Path destination = destPath.resolve(relative.toString());
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.createDirectories(destination.getParent());
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed to extract: " + source, e);
                        }
                    });
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        } catch (IOException e) {
            throw new IOException("Failed to unzip file: " + zipFile.getAbsolutePath(), e);
        }
    }


    public static void deleteFolder(Path folder) {
        if (folder == null || !Files.exists(folder)) return;
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Failed to delete folder '{}': {}", folder, e.getMessage());
        }
    }

    public static LreTestPlanCreationRequest fromGitPath(String gitPath) {
        String normalized = gitPath.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        String name = normalized.substring(lastSlash + 1);
        String folder = lastSlash > 0 ? normalized.substring(0, lastSlash) : "Gitlab";
        return new LreTestPlanCreationRequest(folder, name);
    }

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        return dt.format(formatter);
    }

    public static String calculateTestDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "N/A";
        Duration duration = Duration.between(start, end);
        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    public static void saveHtmlReport(String htmlContent, Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, htmlContent);
            log.info("Emailable report is created at {}", filePath);
        } catch (IOException e) {
            throw new LreException("Failed to save HTML report: " + filePath, e);
        }
    }
}
