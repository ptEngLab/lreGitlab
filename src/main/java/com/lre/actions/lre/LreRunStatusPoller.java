package com.lre.actions.lre;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.common.entities.base.run.LreRunStatus;
import com.lre.actions.common.entities.base.run.PostRunAction;
import com.lre.actions.common.entities.base.run.RunState;
import com.lre.actions.runmodel.LreTestRunModel;
import lombok.extern.slf4j.Slf4j;

import static com.lre.actions.helpers.ConfigConstants.MAX_RETRIES;

@Slf4j
public class LreRunStatusPoller {

    private static final int RETRY_DELAY_MILLIS = 5000;
    private static final long DEFAULT_POLL_INTERVAL_MILLIS = 30000;
    private static final long MILLIS_PER_MINUTE = 60_000L;

    private final LreRestApis apiClient;
    private final LreAuthenticationManager authManager;
    private final int runId;
    private final PostRunAction postRunAction;
    private final long pollIntervalMillis;
    private final long timeslotDurationMillis;

    public LreRunStatusPoller(LreRestApis apiClient, LreTestRunModel model, int timeslotDurationInMinutes) {
        this.apiClient = apiClient;
        this.authManager = new LreAuthenticationManager(apiClient, model);
        this.runId = model.getRunId();
        this.postRunAction = model.getLrePostRunAction();
        this.pollIntervalMillis = DEFAULT_POLL_INTERVAL_MILLIS;
        this.timeslotDurationMillis = timeslotDurationInMinutes * MILLIS_PER_MINUTE;
    }

    public LreRunStatus pollUntilDone() {
        long startTime = System.currentTimeMillis();
        RunState lastLoggedState = RunState.UNDEFINED;
        int consecutiveFailures = 0;


        while (!Thread.currentThread().isInterrupted()) {
            try {
                LreRunStatus currentStatus = apiClient.getRunStatus(runId);
                RunState currentState = RunState.fromValue(currentStatus.getRunState());

                // Log state changes
                if (currentState != lastLoggedState) {
                    logStateChange(currentState, startTime);
                    lastLoggedState = currentState;
                }

                // Check for terminal state according to postRunAction
                if (postRunAction.isTerminal(currentState)) {
                    log.info("Run [{}] reached terminal state [{}] for PostRunAction [{}]", runId, currentState, postRunAction);
                    return currentStatus;
                }

                consecutiveFailures = 0; // Reset on successful fetch

                // Exit loop if timeslot duration exceeded
                if (System.currentTimeMillis() - startTime > timeslotDurationMillis) {
                    log.info("Timeslot duration ended for run [{}], stopping monitoring", runId);
                    break;
                }

                sleep(pollIntervalMillis);

            } catch (Exception e) {
                consecutiveFailures++;
                log.warn("Failed to fetch run status for run [{}]: {}", runId, e.getMessage());

                if (consecutiveFailures >= MAX_RETRIES) {
                    log.info("Maximum retries reached, reauthenticating for run [{}]", runId);
                    authManager.login();
                    consecutiveFailures = 0;
                }

                sleep(RETRY_DELAY_MILLIS);
            }
        }

        // Return the last known status after exiting the loop
        try {
            LreRunStatus finalStatus = apiClient.getRunStatus(runId);
            log.info("Final run state for [{}]: {}", runId, finalStatus.getRunState());
            return finalStatus;
        } catch (Exception e) {
            log.warn("Failed to fetch final run status for [{}]: {}", runId, e.getMessage());
            LreRunStatus unknownStatus = new LreRunStatus();
            unknownStatus.setRunState(RunState.UNDEFINED.getValue());
            return unknownStatus;
        }
    }
    private void logStateChange(RunState state, long startTime) {
        long now = System.currentTimeMillis();
        long elapsedSeconds = (now - startTime) / 1000;
        long remainingMinutes = (timeslotDurationMillis - (now - startTime)) / MILLIS_PER_MINUTE;

        String formattedLog = String.format(
                "| %-10s | %-35s | %-25s | %-14s |",
                "RunId: " + runId,
                "State: " + state.getValue(),
                "Elapsed: " + elapsedSeconds + " seconds",
                "Time remaining: " + Math.max(0, remainingMinutes) + " minutes"
        );

        log.info(formattedLog);
    }


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Polling interrupted for run [{}]", runId);
        }
    }

}