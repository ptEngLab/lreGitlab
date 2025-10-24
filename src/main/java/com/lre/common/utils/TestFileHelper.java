package com.lre.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.lre.common.constants.ConfigConstants.DEFAULT_TEST_FOLDER;

@Slf4j
public class TestFileHelper {

    // DTO to hold test file details
    public record TestFileDetails(String name, String folder, String content) { }

    public static TestFileDetails getTestFileDetails(Path workspacePath, String testValue) {
        Path filePath = resolveTestFilePath(workspacePath, testValue);
        return new TestFileDetails(
                extractTestName(filePath),
                extractTestFolder(filePath, workspacePath),
                readTestFileContent(filePath)
        );
    }

    public static Path resolveTestFilePath(Path workspacePath, String testValue) {
        Path filePath = workspacePath.resolve(testValue).normalize();
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            log.error("Test file not found or invalid: {}", filePath);
            throw new IllegalArgumentException("Test file not found or invalid: " + filePath);
        }
        return filePath;
    }

    public static String readTestFileContent(Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read test file: {}", filePath, e);
            throw new RuntimeException("Failed to read test file: " + filePath, e);
        }
    }

    public static String extractTestFolder(Path filePath, Path workspacePath) {
        Path parentPath = filePath.getParent();
        if (parentPath == null || parentPath.equals(workspacePath)) return DEFAULT_TEST_FOLDER;
        Path relativeFolder = workspacePath.relativize(parentPath);
        return relativeFolder.toString();
    }

    public static String extractTestName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    public static boolean isYamlTest(String testValue) {
        String lower = testValue.trim().toLowerCase();
        return lower.endsWith(".yaml") || lower.endsWith(".yml");
    }
}
