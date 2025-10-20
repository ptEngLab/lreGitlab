package com.lre.model.git;

public record GitToLreUploadResult(String scriptName, String commitSha, String status, String message) {}
