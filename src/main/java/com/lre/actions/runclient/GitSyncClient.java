package com.lre.actions.runclient;

import com.lre.actions.apis.GitLabRestApis;
import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.GitTestRunModel;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.model.git.GitLabCommit;
import com.lre.services.LreAuthenticationManager;
import com.lre.services.git.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.lre.actions.utils.ConfigConstants.COMMIT_HISTORY_ARTIFACT_PATH;
import static com.lre.actions.utils.ConfigConstants.DEFAULT_OUTPUT_DIR;

@Slf4j
public class GitSyncClient implements AutoCloseable {

    private final LreAuthenticationManager authManager;
    private final GitRepositoryScanner scanner;
    private final CommitHistoryManager historyManager;
    private final SyncAnalyzer analyzer;
    private final LreSyncService lreService;

    public GitSyncClient(GitTestRunModel gitModel, LreTestRunModel lreModel) {
        LreRestApis lreApis = new LreRestApis(lreModel);
        GitLabRestApis gitApis = new GitLabRestApis(gitModel);

        this.authManager = new LreAuthenticationManager(lreApis, lreModel);
        this.scanner = new GitRepositoryScanner(gitApis, 5);
        this.historyManager = new CommitHistoryManager(gitApis, getHistoryPath());
        this.analyzer = new SyncAnalyzer();
        this.lreService = new LreSyncService(gitApis, lreModel, lreApis);

        this.authManager.login();
    }

    public boolean sync() throws LreException {
        List<GitLabCommit> current = scanner.scanScripts();
        List<GitLabCommit> previous = historyManager.loadHistory();

        boolean success;
        if (previous.isEmpty()) {
            log.info("Performing INITIAL sync with {} scripts", current.size());
            success = lreService.uploadScripts(current);
        } else {
            log.info("Performing INCREMENTAL sync");
            SyncResult diff = analyzer.analyze(previous, current);
            if (!diff.hasChanges()) {
                log.info("No changes detected - skipping sync");
                return true;
            }
            logSyncSummary(diff);
            success = lreService.deleteScripts(diff.scriptsToDelete()) && lreService.uploadScripts(diff.scriptsToUpload());
        }

        if (success) historyManager.saveHistory(current);

        return success;
    }

    private void logSyncSummary(SyncResult result) {
        log.info("SYNC SUMMARY: upload={} delete={} unchanged={}",
                result.scriptsToUpload().size(),
                result.scriptsToDelete().size(),
                result.unchangedScripts().size());
    }

    private Path getHistoryPath() {
        return Paths.get(DEFAULT_OUTPUT_DIR, COMMIT_HISTORY_ARTIFACT_PATH);
    }

    @Override
    public void close() {
        try {
            authManager.close();
        } catch (Exception e) {
            log.warn("Error during cleanup", e);
        }
    }


}
