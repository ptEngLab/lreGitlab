package com.lre.services.git;

import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.CommonUtils;
import com.lre.model.git.GitLabCommit;
import com.lre.model.git.GitToLreSyncResult;
import com.lre.model.testplan.LreTestPlanCreationRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.lre.actions.utils.CommonUtils.logTable;
import static com.lre.actions.utils.CommonUtils.normalizePathWithSubject;
import static org.apache.commons.lang3.StringUtils.truncate;

/**
 * Orchestrates syncing GitLab scripts with LoadRunner Enterprise.
 * Delegates packaging and upload logic to smaller, focused helpers.
 */
@Slf4j
public record LreSyncService(GitScriptPackager scriptPackager, LreTestRunModel lreModel,
                             LreScriptManager scriptManager) {
    /**
     * Uploads scripts to LRE. Each script is fetched from GitLab, packaged, and uploaded.
     */
    public boolean uploadScripts(List<GitLabCommit> commits) {
        if (commits == null || commits.isEmpty()) {
            log.info("No scripts to upload.");
            return true;
        }

        boolean allSuccessful = true;
        List<GitToLreSyncResult> results = new ArrayList<>();

        for (GitLabCommit commit : commits) {
            String scriptName = commit.getPath();
            String commitSha = commit.getSha().substring(0, Math.min(8, commit.getSha().length()));

            try {
                deriveTestPlanDetails(commit);
                log.info("Preparing script for upload: {}", scriptName);
                Path scriptZip = scriptPackager.prepare(commit);
                scriptManager.upload(lreModel, scriptZip);
                log.debug("Successfully uploaded script: {}", lreModel.getTestName());
                results.add(new GitToLreSyncResult(lreModel.getTestFolderPath(), lreModel.getTestName(), commitSha,
                        "UPLOAD", "SUCCESS", "Uploaded successfully"));

                scriptPackager.cleanupCommitTempDir(scriptZip.getParent().getParent());

            } catch (Exception e) {
                log.error("Failed to upload script '{}': {}", commit.getPath(), e.getMessage(), e);
                results.add(new GitToLreSyncResult(lreModel.getTestFolderPath(), lreModel.getTestName(), commitSha,
                        "UPLOAD", "FAILED", e.getMessage()));
                allSuccessful = false;
            }
        }

        logSyncSummary(results);

        return allSuccessful;
    }

    /**
     * Deletes scripts in LRE that no longer exist in Git.
     */
    public boolean deleteScripts(List<GitLabCommit> commits) {
        if (commits == null || commits.isEmpty()) {
            log.info("No scripts to delete.");
            return true;
        }

        boolean allDeleted = true;
        List<GitToLreSyncResult> results = new ArrayList<>();

        for (GitLabCommit commit : commits) {
            String commitSha = commit.getSha().substring(0, Math.min(8, commit.getSha().length()));

            try {
                LreTestPlanCreationRequest info = CommonUtils.fromGitPath(commit.getPath());
                String normalizedPath = normalizePathWithSubject(info.getPath());
                scriptManager.delete(normalizedPath, info.getName()); // using the new method
                results.add(new GitToLreSyncResult(lreModel.getTestFolderPath(), lreModel.getTestName(), commitSha,
                        "DELETE", "SUCCESS", "Deleted successfully"));
            } catch (Exception e) {
                log.error("Failed to delete script '{}' from LRE: {}", commit.getPath(), e.getMessage());
                results.add(new GitToLreSyncResult(lreModel.getTestFolderPath(), lreModel.getTestName(), commitSha,
                        "DELETE", "FAILED", e.getMessage()));
                allDeleted = false;
            }
        }

        logSyncSummary(results);

        return allDeleted;
    }

    private void deriveTestPlanDetails(GitLabCommit commit) {
        // Derive folder/name from commit path
        LreTestPlanCreationRequest info = CommonUtils.fromGitPath(commit.getPath());
        lreModel.setTestFolderPath(normalizePathWithSubject(info.getPath()));
        lreModel.setTestName(info.getName());

    }


    private void logSyncSummary(List<GitToLreSyncResult> results) {
        if (results == null || results.isEmpty()) {
            log.info("No script uploads / delete to summarize.");
            return;
        }

        // Define header with serial number
        String[] header = {"#", "Test Folder Path", "Script Name", "Commit", "Action", "Status", "Message"};

        // Prepare data rows
        String[][] dataRows = new String[results.size()][header.length];
        int i = 0;
        for (GitToLreSyncResult result : results) {
            dataRows[i] = new String[]{
                    String.valueOf(i + 1),
                    result.testFolderPath(),
                    result.scriptName(),
                    result.commitSha(),
                    result.action(),
                    result.status(),
                    truncate(result.message(), 80)
            };
            i++;
        }

        // Log the table
        log.info(logTable(header, dataRows));
    }

}
