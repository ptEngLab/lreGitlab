package com.lre.services.git;

import com.lre.actions.apis.GitLabRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.utils.JsonUtils;
import com.lre.model.git.GitLabCommit;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

@Slf4j
public record CommitHistoryManager(GitLabRestApis gitLabRestApis, Path historyFile) {

    public List<GitLabCommit> loadHistory() {
        try {
            Files.createDirectories(historyFile.getParent());
            if (downloadFromGitLab()) {
                return readFromFile();
            }
            log.info("No commit history found, starting fresh");
            return List.of();
        } catch (IOException e) {
            log.warn("Failed to load commit history", e);
            return List.of();
        }
    }

    public void saveHistory(List<GitLabCommit> commits) {
        try {
            Files.createDirectories(historyFile.getParent());
            String json = JsonUtils.toJson(commits);
            Path tempFile = Files.createTempFile(historyFile.getParent(), "commit-history", ".tmp");
            Files.writeString(tempFile, json, StandardCharsets.UTF_8);
            Files.move(tempFile, historyFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Saved commit history for {} scripts", commits.size());
        } catch (IOException e) {
            throw new LreException("Failed to save commit history", e);
        }
    }

    private boolean downloadFromGitLab() {
        try {
            return gitLabRestApis.downloadGitCommitHistoryArtifact(historyFile.toString());
        } catch (LreException e) {
            if (isNotFoundError(e)) {
                log.debug("Commit history not found in GitLab");
                return false;
            }
            throw e;
        }
    }

    private boolean isNotFoundError(LreException e) {
        String errorDetails = e.getMessage() + " " + (e.getCause() != null ? e.getCause().getMessage() : "");
        return errorDetails.toLowerCase().contains("status=404");
    }

    private List<GitLabCommit> readFromFile() throws IOException {
        String json = Files.readString(historyFile, StandardCharsets.UTF_8);
        return JsonUtils.fromJsonArray(json, GitLabCommit.class);
    }
}
