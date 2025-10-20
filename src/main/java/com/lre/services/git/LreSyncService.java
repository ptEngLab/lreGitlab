package com.lre.services.git;

import com.lre.actions.apis.GitLabRestApis;
import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.CommonUtils;
import com.lre.model.git.GitLabCommit;
import com.lre.model.git.GitToLreUploadResult;
import com.lre.model.testplan.LreTestPlanCreationRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.lre.actions.utils.CommonUtils.logTable;
import static org.apache.commons.lang3.StringUtils.truncate;

/**
 * Orchestrates syncing GitLab scripts with LoadRunner Enterprise.
 * Delegates packaging and upload logic to smaller, focused helpers.
 */
@Slf4j
public class LreSyncService {

    private final GitLabRestApis gitLabRestApis;
    private final LreRestApis lreRestApis;
    private final LreTestRunModel lreModel;

    private final GitScriptPackager scriptPackager;
    private final LreUploader uploader;

    public LreSyncService(GitLabRestApis gitLabRestApis, LreTestRunModel lreModel, LreRestApis lreRestApis) {
        this.gitLabRestApis = gitLabRestApis;
        this.lreRestApis = lreRestApis;
        this.lreModel = lreModel;
        this.scriptPackager = new GitScriptPackager(gitLabRestApis);
        this.uploader = new LreUploader(lreRestApis);
    }

    /**
     * Uploads scripts to LRE. Each script is fetched from GitLab, packaged, and uploaded.
     */
    public boolean uploadScripts(List<GitLabCommit> commits) {
        if (commits == null || commits.isEmpty()) {
            log.info("No scripts to upload.");
            return true;
        }

        boolean allSuccessful = true;
        List<GitToLreUploadResult> results = new ArrayList<>();

        for (GitLabCommit commit : commits) {
            String scriptName = commit.getPath();
            String commitSha = commit.getSha().substring(0, Math.min(8, commit.getSha().length()));

            try {
                log.info("Preparing script for upload: {}", scriptName);
                Path scriptZip = scriptPackager.prepare(commit);

                deriveTestPlanDetails(commit);

                uploader.upload(lreModel, scriptZip);
                log.debug("Successfully uploaded script: {}", lreModel.getTestName());
                results.add(new GitToLreUploadResult(scriptName, commitSha, "SUCCESS", "Uploaded successfully"));


                scriptPackager.cleanupCommitTempDir(scriptZip.getParent().getParent());

            } catch (Exception e) {
                log.error("Failed to upload script '{}': {}", commit.getPath(), e.getMessage(), e);
                results.add(new GitToLreUploadResult(scriptName, commitSha, "FAILED", e.getMessage()));

                allSuccessful = false;
            }
        }

        logUploadSummary(results);

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
            try {
                LreTestPlanCreationRequest info = CommonUtils.fromGitPath(commit.getPath());
                // TODO
//                lreRestApis.deleteTestByPath(info.getPath(), info.getName());
                log.info("Deleted script from LRE: {}", info.getName());
            } catch (LreException e) {
                log.warn("Failed to delete script '{}' from LRE: {}", commit.getPath(), e.getMessage());
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    private void deriveTestPlanDetails(GitLabCommit commit) {
        // Derive folder/name from commit path
        LreTestPlanCreationRequest info = CommonUtils.fromGitPath(commit.getPath());
        lreModel.setTestFolderPath(info.getPath());
        lreModel.setTestName(info.getName());

    }


    private void logUploadSummary(List<GitToLreUploadResult> results) {
        if (results == null || results.isEmpty()) {
            log.info("No script uploads to summarize.");
            return;
        }

        // Define header separately
        String[] header = { "Script Name", "Commit", "Status", "Message" };

        // Prepare data rows
        String[][] dataRows = new String[results.size()][4];
        int i = 0;
        for (GitToLreUploadResult result : results) {
            dataRows[i++] = new String[]{
                    result.scriptName(),
                    result.commitSha(),
                    result.status(),
                    truncate(result.message(), 40)
            };
        }

        // Log the table
        log.info(logTable(header, dataRows));
    }

}
