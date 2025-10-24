package com.lre.services.lre;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatus;
import com.lre.model.enums.PostRunAction;
import com.lre.model.enums.RunState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.lre.common.constants.ConfigConstants.*;

@Slf4j
public class LreRunStatusPoller {
    private long lastLogTime = 0;
    private static final long LOG_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(5); // every 5 minutes

    private RunState lastState = RunState.UNDEFINED; // track globally

    private final LreRestApis apiClient;
    private final LreAuthenticationManager authManager;
    private final int runId;
    private final PostRunAction postRunAction;
    private final long pollIntervalSeconds;
    private final long timeslotDurationMillis;
    private final LreTestRunModel model;

    private static final RunState[] PROGRESS_STATES = {
            RunState.INITIALIZING,
            RunState.RUNNING,
            RunState.BEFORE_COLLATING_RESULTS,
            RunState.COLLATING_RESULTS,
            RunState.BEFORE_CREATING_ANALYSIS_DATA,
            RunState.CREATING_ANALYSIS_DATA,
            RunState.FINISHED
    };

    public LreRunStatusPoller(LreRestApis apiClient, LreTestRunModel model, int timeslotDurationInMinutes) {
        this.apiClient = apiClient;
        this.model = model;
        this.authManager = new LreAuthenticationManager(apiClient, model);
        this.runId = model.getRunId();
        this.postRunAction = model.getLrePostRunAction();
        this.pollIntervalSeconds = DEFAULT_POLL_INTERVAL_SECONDS;
        this.timeslotDurationMillis = TimeUnit.MINUTES.toMillis(timeslotDurationInMinutes);
    }

    public LreRunStatus pollUntilDone() {
        long startTime = System.currentTimeMillis();
//        RunState lastState = RunState.UNDEFINED;
        int consecutiveFailures = 0;
        LreRunStatus lastKnownStatus = new LreRunStatus();
        lastKnownStatus.setRunState(RunState.UNDEFINED.getValue());

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 🔹 Fetch current status
                LreRunStatus currentStatus = fetchStatusAndLogTransition(startTime);
                RunState currentState = RunState.fromValue(currentStatus.getRunState());
                lastKnownStatus = currentStatus; // store last known status
                lastState = currentState;

                // 🔹 Handle terminal completion
                if (isTerminalState(currentState)) return currentStatus;

                // 🔹 Monitor error thresholds
                if (shouldAbortDueToErrors(currentStatus, currentState)) {
                    abortRunDueToErrors(currentStatus);
                    break;
                }

                // 🔹 Handle timeslot expiry
                if (isTimeslotExceeded(startTime)) {
                    log.info("Timeslot duration ended for run [{}], stopping monitoring", runId);
                    break;
                }

                consecutiveFailures = 0; // reset after successful fetch
                sleep(pollIntervalSeconds);

            } catch (Exception e) {
                consecutiveFailures = handleFailure(consecutiveFailures, e);
            }
        }

        // Return the last known status if loop exited due to timeslot expiration
        log.debug("Returning last known status for run [{}]: {}", runId, lastKnownStatus.getRunState());
        return lastKnownStatus;
    }

    private LreRunStatus fetchStatusAndLogTransition(long startTime) {
        LreRunStatus currentStatus = apiClient.fetchRunStatus(runId);
        RunState currentState = RunState.fromValue(currentStatus.getRunState());
        logHybrid(currentState, startTime);
        return currentStatus;
    }


    private boolean isTerminalState(RunState currentState) {
        if (!postRunAction.isTerminal(currentState)) return false;
        log.info("Run [{}] reached terminal state [{}] for PostRunAction [{}]", runId, currentState, postRunAction);
        if (currentState == RunState.FINISHED) model.setHtmlReportAvailable(true);
        return true;
    }

    private boolean shouldAbortDueToErrors(LreRunStatus currentStatus, RunState currentState) {
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

    private void abortRunDueToErrors(LreRunStatus currentStatus) {
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

    private int handleFailure(int consecutiveFailures, Exception e) {
        consecutiveFailures++;
        log.warn("Failed to fetch status for run [{}]: {} (Attempt {}/{})", runId, e.getMessage(), consecutiveFailures, MAX_RETRIES);

        if (consecutiveFailures >= MAX_RETRIES) {
            log.info("Max retries reached. Reauthenticating for run [{}].", runId);
            authManager.login();
            consecutiveFailures = 0;
        }

        sleep(RETRY_DELAY_SECONDS);
        return consecutiveFailures;
    }

    private boolean isTimeslotExceeded(long startTime) {
        return System.currentTimeMillis() - startTime > timeslotDurationMillis;
    }

    private void logHybrid(RunState state, long startTime) {
        long now = System.currentTimeMillis();

        if (state != lastState || now - lastLogTime >= LOG_INTERVAL_MILLIS) {
            long elapsedMillis = now - startTime;
            long remainingMillis = Math.max(0, timeslotDurationMillis - elapsedMillis);

            int progress = calculateProgress(state, elapsedMillis);

            String formattedLog = String.format(
                    "| %-10s | %-40s | %-25s | %-14s | %-14s | %-20s |",
                    "RunId: " + runId,
                    "State: " + state.getValue(),
                    "Elapsed: " + formatDuration(elapsedMillis),
                    "Timeslot: " + formatDuration(timeslotDurationMillis),
                    "Time remaining: " + formatDuration(remainingMillis),
                    String.format("%3d%% %s", progress, buildProgressBar(progress))
            );

            log.info(formattedLog);
            lastState = state;
            lastLogTime = now;
        }
    }

    private int calculateProgress(RunState state, long elapsedMillis) {
        // Find index of current state in ordered list
        int index = 0;
        for (int i = 0; i < PROGRESS_STATES.length; i++) {
            if (PROGRESS_STATES[i] == state) {
                index = i;
                break;
            }
        }

        // Progress by state fraction
        int progressByState = (int) ((index + 1) * 100.0 / PROGRESS_STATES.length);

        // Progress by timeslot (optional for long runs)
        int progressByTime = (int) Math.min(100, (elapsedMillis * 100) / timeslotDurationMillis);

        // Use whichever is greater for a meaningful progress bar
        return Math.max(progressByState, progressByTime);
    }


    private String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String buildProgressBar(int percent) {
        int barWidth = 20;
        int filledBlocks = (percent * barWidth) / 100;
        return "█".repeat(filledBlocks) + "░".repeat(barWidth - filledBlocks);
    }

    private void sleep(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Polling interrupted for run [{}]", runId);
        }
    }

}
