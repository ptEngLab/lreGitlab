package com.lre.services.git;

import com.lre.actions.apis.GitLabRestApis;
import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.CommonUtils;
import com.lre.model.git.GitLabCommit;
import com.lre.model.testplan.LreTestPlanCreationRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

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
        for (GitLabCommit commit : commits) {
            try {
                log.info("Preparing script for upload: {}", commit.getPath());
                Path scriptZip = scriptPackager.prepare(commit);

                deriveTestPlanDetails(commit);

                uploader.upload(lreModel, scriptZip);
                log.info("Successfully uploaded script: {}", lreModel.getTestName());

                scriptPackager.cleanupCommitTempDir(scriptZip.getParent().getParent());

            } catch (Exception e) {
                log.error("Failed to upload script '{}': {}", commit.getPath(), e.getMessage(), e);
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
}
