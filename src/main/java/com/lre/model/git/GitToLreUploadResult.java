package com.lre.model.git;

public record GitToLreUploadResult(String testFolderPath, String scriptName, String commitSha, String status, String message) {}
