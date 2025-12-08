package com.lre.services.lre.poller;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.enums.PostRunAction;
import com.lre.model.enums.RunState;
import com.lre.model.run.LreRunStatus;
import com.lre.services.lre.auth.LreAuthenticationManager;
import com.lre.services.lre.progress.RunProgressCalculator;
import com.lre.services.lre.formatter.RunStatusFormatter;
import com.lre.services.lre.monitor.RunStatusMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.lre.common.constants.ConfigConstants.*;
import static com.lre.common.utils.CommonUtils.formatDuration;

@Slf4j
public class LreRunStatusPoller {
    private long lastLogTime = 0;
    private RunState lastState = RunState.UNDEFINED;

    private final RunStatusMonitor statusMonitor;
    private final RunProgressCalculator progressCalculator;
    private final RunStatusFormatter statusFormatter;
    private final LreAuthenticationManager authManager;
    private final PostRunAction postRunAction;
    private final long pollIntervalSeconds;
    private final long timeslotDurationMillis;
    private final LreTestRunModel model;

    // Dynamic logging intervals
    private static final long SHORT_RUN_THRESHOLD = TimeUnit.MINUTES.toMillis(30);
    private static final long MEDIUM_RUN_THRESHOLD = TimeUnit.HOURS.toMillis(2);
    private static final long LOG_INTERVAL_SHORT = TimeUnit.MINUTES.toMillis(1);
    private static final long LOG_INTERVAL_MEDIUM = TimeUnit.MINUTES.toMillis(3);
    private static final long LOG_INTERVAL_LONG = TimeUnit.MINUTES.toMillis(5);

    public LreRunStatusPoller(LreRestApis apiClient, LreTestRunModel model, int timeslotDurationInMinutes) {
        this.timeslotDurationMillis = TimeUnit.MINUTES.toMillis(timeslotDurationInMinutes);
        this.model = model;
        this.statusMonitor = new RunStatusMonitor(apiClient, model);
        this.progressCalculator = new RunProgressCalculator(timeslotDurationMillis);
        this.statusFormatter = new RunStatusFormatter(model.getRunId(), timeslotDurationMillis);
        this.authManager = new LreAuthenticationManager(apiClient, model);
        this.postRunAction = model.getLrePostRunAction();
        this.pollIntervalSeconds = DEFAULT_POLL_INTERVAL_SECONDS;
    }

    public LreRunStatus pollUntilDone() {
        long startTime = System.currentTimeMillis();
        int consecutiveFailures = 0;
        LreRunStatus lastKnownStatus = createInitialStatus();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                LreRunStatus currentStatus = statusMonitor.fetchCurrentStatus();
                RunState currentState = RunState.fromValue(currentStatus.getRunState());
                lastKnownStatus = currentStatus;

                logStatus(currentState, startTime);

                if (isTerminalState(currentState)) return currentStatus;
                if (statusMonitor.shouldAbortDueToErrors(currentStatus, currentState)) {
                    statusMonitor.abortRunDueToErrors(currentStatus);
                    break;
                }
                if (isTimeslotExceeded(startTime)) {
                    logTimeslotExceeded();
                    break;
                }

                consecutiveFailures = 0;
                sleep(pollIntervalSeconds);

            } catch (Exception e) {
                consecutiveFailures = handleFailure(consecutiveFailures, e);
            }
        }

        log.debug("Returning last known status for run [{}]: {}", model.getRunId(), lastKnownStatus.getRunState());
        return lastKnownStatus;
    }

    private LreRunStatus createInitialStatus() {
        LreRunStatus status = new LreRunStatus();
        status.setRunState(RunState.UNDEFINED.getValue());
        return status;
    }

    private boolean isTerminalState(RunState currentState) {
        if (!postRunAction.isTerminal(currentState)) return false;
        log.info("Run [{}] reached terminal state [{}] for PostRunAction [{}]", model.getRunId(), currentState, postRunAction);
        if (currentState == RunState.FINISHED) {
            model.setHtmlReportAvailable(true);
            model.setAnalysedReportAvailable(true);

        }
        return true;
    }

    private boolean isTimeslotExceeded(long startTime) {
        return System.currentTimeMillis() - startTime > timeslotDurationMillis;
    }

    private void logTimeslotExceeded() {
        log.info("Run [{}] reached timeslot limit ({}). Stopping monitoring.",
                model.getRunId(), formatDuration(timeslotDurationMillis));
    }

    private void logStatus(RunState state, long startTime) {
        long now = System.currentTimeMillis();
        long elapsedMillis = now - startTime;

        if (shouldLog(state, now)) {
            long remainingMillis = Math.max(0, timeslotDurationMillis - elapsedMillis);
            int progress = progressCalculator.calculateProgress(state, elapsedMillis); // Pass elapsed time

            String progressBar = progressCalculator.buildProgressBar(progress);
            String logMessage = statusFormatter.formatStatusLog(state, elapsedMillis, remainingMillis, progress, progressBar);
            log.info(logMessage);

            lastState = state;
            lastLogTime = now;
        }
    }

    private boolean shouldLog(RunState state, long currentTime) {
        return state != lastState || currentTime - lastLogTime >= getDynamicLogInterval();
    }

    private long getDynamicLogInterval() {
        if (timeslotDurationMillis <= SHORT_RUN_THRESHOLD) return LOG_INTERVAL_SHORT;
        else if (timeslotDurationMillis <= MEDIUM_RUN_THRESHOLD) return LOG_INTERVAL_MEDIUM;
        else return LOG_INTERVAL_LONG;
    }

    private int handleFailure(int consecutiveFailures, Exception e) {
        consecutiveFailures++;
        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        log.warn("Failed to fetch status for run [{}]: {} (Attempt {}/{})",
                model.getRunId(), msg, consecutiveFailures, MAX_RETRIES);

        if (consecutiveFailures >= MAX_RETRIES) {
            log.info("Max retries reached. Reauthenticating for run [{}].", model.getRunId());
            authManager.login();
            consecutiveFailures = 0;
        }

        sleep(RETRY_DELAY_SECONDS);
        return consecutiveFailures;
    }

    private void sleep(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Polling interrupted for run [{}]", model.getRunId());
        }
    }
}