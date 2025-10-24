package com.lre.services.lre.monitor;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.enums.RunState;
import com.lre.model.run.LreRunStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunStatusMonitor {
    private final LreRestApis apiClient;
    private final int runId;
    private final LreTestRunModel model;

    public RunStatusMonitor(LreRestApis apiClient, LreTestRunModel model) {
        this.apiClient = apiClient;
        this.runId = model.getRunId();
        this.model = model;
    }

    public LreRunStatus fetchCurrentStatus() {
        return apiClient.fetchRunStatus(runId);
    }

    public boolean shouldAbortDueToErrors(LreRunStatus currentStatus, RunState currentState) {
        if (currentState != RunState.RUNNING) return false;

        long errorCount = currentStatus.getTotalErrors();
        long failedTxnCount = currentStatus.getTotalFailedTransactions();
        long errorThreshold = model.getMaxErrors();
        long failedTxnThreshold = model.getMaxFailedTxns();

        boolean errorLimitExceeded = errorCount >= errorThreshold;
        boolean failedTxnLimitExceeded = failedTxnCount >= failedTxnThreshold;

        if (errorLimitExceeded || failedTxnLimitExceeded) {
            log.warn("Run [{}] threshold breach detected. Current Errors: {}/{} | Failed Txns: {}/{}",
                    runId, errorCount, errorThreshold, failedTxnCount, failedTxnThreshold);
            return true;
        }

        return false;
    }

    public void abortRunDueToErrors(LreRunStatus currentStatus) {
        long errorCount = currentStatus.getTotalErrors();
        long failedTxnCount = currentStatus.getTotalFailedTransactions();
        long errorThreshold = model.getMaxErrors();
        long failedTxnThreshold = model.getMaxFailedTxns();

        StringBuilder reason = new StringBuilder();

        if (errorCount >= errorThreshold) {
            reason.append(String.format("Error threshold breached (%d/%d)", errorCount, errorThreshold));
        }
        if (failedTxnCount >= failedTxnThreshold) {
            if (!reason.isEmpty()) reason.append(" | ");
            reason.append(String.format("Failed Txn threshold breached (%d/%d)", failedTxnCount, failedTxnThreshold));
        }

        log.error("Run [{}] Aborting test execution due to threshold breach: {}", runId, reason);
        try {
            apiClient.abortRun(runId);
            log.warn("Run aborted due to threshold breach. errorCount={}, errorThreshold={}, failedTxnCount={}, failedTxnThreshold={}",
                    errorCount, errorThreshold, failedTxnCount, failedTxnThreshold);
            model.setTestFailed(true);
            model.setFailureReason("Aborted due to threshold breach: " + reason);
        } catch (Exception ex) {
            log.error("Failed to abort run [{}] after threshold breach: {}", runId, ex.getMessage());
        }
    }
}