package com.lre.services;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatus;
import com.lre.model.enums.PostRunAction;
import com.lre.model.enums.RunState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.lre.actions.utils.ConfigConstants.*;

@Slf4j
public class LreRunStatusPoller {

    private final LreRestApis apiClient;
    private final LreAuthenticationManager authManager;
    private final int runId;
    private final PostRunAction postRunAction;
    private final long pollIntervalSeconds;
    private final long timeslotDurationMillis;
    private final long timeslotDurationInMinutes;
    private final LreTestRunModel model;

    public LreRunStatusPoller(LreRestApis apiClient, LreTestRunModel model, int timeslotDurationInMinutes) {
        this.apiClient = apiClient;
        this.model = model;
        this.authManager = new LreAuthenticationManager(apiClient, model);
        this.runId = model.getRunId();
        this.postRunAction = model.getLrePostRunAction();
        this.pollIntervalSeconds = DEFAULT_POLL_INTERVAL_SECONDS;
        this.timeslotDurationInMinutes = timeslotDurationInMinutes;
        this.timeslotDurationMillis = TimeUnit.MINUTES.toMillis(timeslotDurationInMinutes);
    }

    public LreRunStatus pollUntilDone() {
        long startTime = System.currentTimeMillis();
        RunState lastLoggedState = RunState.UNDEFINED;
        int consecutiveFailures = 0;
        LreRunStatus lastKnownStatus = new LreRunStatus();
        lastKnownStatus.setRunState(RunState.UNDEFINED.getValue());

        long errorCount;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                LreRunStatus currentStatus = apiClient.fetchRunStatus(runId);
                RunState currentState = RunState.fromValue(currentStatus.getRunState());
                lastKnownStatus = currentStatus; // store last known status

                // Log state changes
                if (currentState != lastLoggedState) {
                    logStateChange(currentState, startTime);
                    lastLoggedState = currentState;
                }

                // Terminal state reached
                if (postRunAction.isTerminal(currentState)) {
                    log.info("Run [{}] reached terminal state [{}] for PostRunAction [{}]", runId, currentState, postRunAction);
                    if (currentState == RunState.FINISHED) model.setHtmlReportAvailable(true);
                    return currentStatus; // exit immediately
                }

                // Check for errors from LRE
                errorCount = currentStatus.getTotalErrors();
                if (errorCount >= model.getMaxErrors()) {
                    log.error("Run [{}] exceeded maximum LRE errors [{}]. Stopping test.", runId, errorCount);
                    apiClient.abortRun(model.getRunId());
                    break;
                }

                consecutiveFailures = 0; // reset after successful fetch

                // Timeslot expired
                if (isTimeslotExceeded(startTime)) {
                    log.info("Timeslot duration ended for run [{}], stopping monitoring", runId);
                    break;
                }

                sleep(pollIntervalSeconds);

            } catch (Exception e) {
                consecutiveFailures = handleFailure(consecutiveFailures, e);
            }
        }

        // Return the last known status if loop exited due to timeslot expiration
        log.info("Returning last known status for run [{}]: {}", runId, lastKnownStatus.getRunState());
        return lastKnownStatus;
    }


    private int handleFailure(int consecutiveFailures, Exception e) {
        consecutiveFailures++;
        log.warn("Failed to fetch run status for run [{}]: {}", runId, e.getMessage());

        if (consecutiveFailures >= MAX_RETRIES) {
            log.info("Maximum retries reached, reauthenticating for run [{}]", runId);
            authManager.login();
            consecutiveFailures = 0;
        }

        sleep(RETRY_DELAY_SECONDS);
        return consecutiveFailures;
    }

    private boolean isTimeslotExceeded(long startTime) {
        return System.currentTimeMillis() - startTime > timeslotDurationMillis;
    }

    private void logStateChange(RunState state, long startTime) {
        long now = System.currentTimeMillis();
        long elapsedSeconds = (now - startTime) / 1000;
        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(Math.max(0, timeslotDurationMillis - (now - startTime)));

        String formattedLog = String.format(
                "| %-10s | %-40s | %-25s | %-14s | %-14s |",
                "RunId: " + runId,
                "State: " + state.getValue(),
                "Elapsed: " + elapsedSeconds + " seconds",
                "Timeslot duration: " + timeslotDurationInMinutes + " minutes",
                "Time remaining: " + remainingMinutes + " minutes"
        );

        log.info(formattedLog);
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
