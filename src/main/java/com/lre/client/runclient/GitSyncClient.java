package com.lre.client.runclient;

import com.lre.client.api.gitlab.GitLabRestApis;
import com.lre.client.base.BaseLreClient;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.model.git.GitLabCommit;
import com.lre.services.git.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.COMMIT_HISTORY_ARTIFACT_PATH;
import static com.lre.common.constants.ConfigConstants.DEFAULT_OUTPUT_DIR;

@Slf4j
public class GitSyncClient extends BaseLreClient {

    private static final int DEFAULT_THREAD_POOL_SIZE = 5;

    private final GitRepositoryScanner scanner;
    private final CommitHistoryManager historyManager;
    private final SyncAnalyzer analyzer;
    private final LreSyncService lreService;

    public GitSyncClient(GitTestRunModel gitModel, LreTestRunModel lreModel) {
        super(lreModel);

        GitLabRestApis gitApis = new GitLabRestApis(gitModel);

        this.scanner = new GitRepositoryScanner(gitApis, DEFAULT_THREAD_POOL_SIZE);
        this.historyManager = new CommitHistoryManager(gitApis, getHistoryPath());
        this.analyzer = new SyncAnalyzer();
        this.lreService = new LreSyncService(gitApis, lreModel, lreRestApis);

        trace("GitSyncClient initialized");
    }

    public boolean sync() throws LreException {
        trace("Starting Git–LRE sync");

        List<GitLabCommit> current = scanner.scanScripts();
        List<GitLabCommit> previous = historyManager.loadHistory();

        if (previous.isEmpty()) {
            return performInitialSync(current);
        }

        return performIncrementalSync(previous, current);
    }

    private boolean performInitialSync(List<GitLabCommit> currentCommits) throws LreException {
        log.info("Performing INITIAL sync with {} scripts", currentCommits.size());
        boolean success = lreService.uploadScripts(currentCommits);

        lreService.logCombinedSummary();

        if (success) historyManager.saveHistory(currentCommits);
        return success;
    }

    private boolean performIncrementalSync(List<GitLabCommit> previous, List<GitLabCommit> current) throws LreException {
        log.info("Performing INCREMENTAL sync");

        SyncResult diff = analyzer.analyze(previous, current);

        if (diff.logIfNoChanges()) return true;

        logSyncSummary(diff);

        boolean uploaded = diff.scriptsToUpload().isEmpty() || lreService.uploadScripts(diff.scriptsToUpload());
        boolean deleted  = diff.scriptsToDelete().isEmpty() || lreService.deleteScripts(diff.scriptsToDelete());

        lreService.logCombinedSummary();

        boolean success = uploaded && deleted;

        if (success) historyManager.saveHistory(current);

        return success;
    }

    private void logSyncSummary(SyncResult diff) {
        log.info("SYNC SUMMARY: total={} | upload={} | delete={} | unchanged={}",
                diff.totalScripts(),
                diff.scriptsToUpload().size(),
                diff.scriptsToDelete().size(),
                diff.unchangedScripts().size());
    }

    private static Path getHistoryPath() {
        return Paths.get(DEFAULT_OUTPUT_DIR, COMMIT_HISTORY_ARTIFACT_PATH);
    }
}
