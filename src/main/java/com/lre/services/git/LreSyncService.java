package com.lre.services.git;

import com.lre.actions.apis.GitLabRestApis;
import com.lre.actions.apis.LreRestApis;
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
 * Handles syncing GitLab scripts with LoadRunner Enterprise.
 * Responsible for packaging, uploading, deleting, and summarizing sync results.
 */
@Slf4j
public class LreSyncService {

    private final GitScriptPackager scriptPackager;
    private final LreTestRunModel lreModel;
    private final LreScriptManager scriptManager;
    private final List<GitToLreSyncResult> results = new ArrayList<>();
    private static final int MESSAGE_LIMIT = 200;

    /**
     * Constructs the sync service with required dependencies.
     */
    public LreSyncService(GitLabRestApis gitLabRestApis, LreTestRunModel lreModel, LreRestApis lreRestApis) {
        this.scriptPackager = new GitScriptPackager(gitLabRestApis);
        this.lreModel = lreModel;
        this.scriptManager = new LreScriptManager(lreRestApis);
    }

    /**
     * Uploads scripts from GitLab to LRE.
     */
    public boolean uploadScripts(List<GitLabCommit> commits) {
        if (commits == null || commits.isEmpty()) {
            log.info("No scripts to upload.");
            return true;
        }

        boolean allSuccessful = true;

        for (GitLabCommit commit : commits) {
            deriveTestPlanDetails(commit);
            String scriptName = commit.getPath();
            String folderPath = lreModel.getTestFolderPath();
            String scriptDisplayName = lreModel.getTestName();
            String commitSha = commit.getSha().substring(0, Math.min(8, commit.getSha().length()));

            try {
                log.debug("Preparing script for upload: {}", scriptName);
                Path scriptZip = scriptPackager.prepare(commit);
                scriptManager.upload(lreModel, scriptZip);

                addSyncResult(folderPath, scriptDisplayName, commitSha, SyncAction.UPLOAD, SyncStatus.SUCCESS, null);

                log.debug("Successfully uploaded script: {}", scriptDisplayName);
                scriptPackager.cleanupCommitTempDir(scriptZip.getParent().getParent());

            } catch (Exception e) {
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                log.error("Failed to upload script '{}' (commit {}): {}", scriptName, commitSha, msg, e);
                addSyncResult(folderPath, scriptDisplayName, commitSha, SyncAction.UPLOAD, SyncStatus.FAILED, msg);
                allSuccessful = false;
            }
        }

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

        for (GitLabCommit commit : commits) {
            String commitSha = commit.getSha().substring(0, Math.min(8, commit.getSha().length()));
            LreTestPlanCreationRequest info = CommonUtils.fromGitPath(commit.getPath());
            String normalizedPath = normalizePathWithSubject(info.getPath());
            String scriptName = info.getName();

            try {
                scriptManager.delete(normalizedPath, scriptName);
                addSyncResult(normalizedPath, scriptName, commitSha, SyncAction.DELETE, SyncStatus.SUCCESS, null);

            } catch (Exception e) {
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                log.error("Failed to delete script '{}' (commit {}): {}", commit.getPath(), commitSha, msg);
                addSyncResult(normalizedPath, scriptName, commitSha, SyncAction.DELETE, SyncStatus.FAILED, msg);
                allDeleted = false;
            }
        }

        return allDeleted;
    }

    /**
     * Derives test plan details from the Git path and updates LRE model.
     */
    private void deriveTestPlanDetails(GitLabCommit commit) {
        LreTestPlanCreationRequest info = CommonUtils.fromGitPath(commit.getPath());
        lreModel.setTestFolderPath(normalizePathWithSubject(info.getPath()));
        lreModel.setTestName(info.getName());
    }

    /**
     * Logs a formatted summary of all sync operations.
     */
    public void logCombinedSummary() {
        if (results.isEmpty()) {
            log.info("No sync operations to summarize.");
            return;
        }

        String[] header = {"#", "Test Folder Path", "Script Name", "Commit", "Action", "Status", "Message"};
        String[][] dataRows = new String[results.size()][header.length];

        for (int i = 0; i < results.size(); i++) {
            GitToLreSyncResult result = results.get(i);
            dataRows[i] = new String[]{
                    String.valueOf(i + 1),
                    result.testFolderPath(),
                    result.scriptName(),
                    result.commitSha(),
                    result.action(),
                    result.status(),
                    truncate(result.message(), MESSAGE_LIMIT)
            };
        }

        log.info("Git to LRE Sync Summary ({} operations):", results.size());
        log.info(logTable(header, dataRows));

        results.clear(); // Reset after logging
    }

    /**
     * Adds a sync result to the list.
     */
    private void addSyncResult(String folderPath, String scriptName, String commitSha,
                               SyncAction action, SyncStatus status, String message) {
        String finalMessage = switch (status) {
            case SUCCESS -> action == SyncAction.UPLOAD ? "Uploaded successfully" : "Deleted successfully";
            case FAILED -> truncate(message != null ? message : "Unknown error", MESSAGE_LIMIT);
        };

        results.add(new GitToLreSyncResult(folderPath, scriptName, commitSha,
                action.name(), status.name(), finalMessage));
    }

    /**
     * Defines the type of synchronization operation.
     */
    private enum SyncAction {UPLOAD, DELETE}


    /**
     * Enum for sync operation outcomes.
     */
    private enum SyncStatus {SUCCESS, FAILED}
}
