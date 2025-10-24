package com.lre.model.git;

public record GitToLreSyncResult(String testFolderPath, String scriptName, String commitSha, String action, String status, String message) {}
