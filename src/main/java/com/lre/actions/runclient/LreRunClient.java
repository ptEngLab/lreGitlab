package com.lre.actions.runclient;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.common.entities.base.run.LreRunStatus;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.lre.*;
import com.lre.actions.runmodel.LreTestRunModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LreRunClient implements AutoCloseable {

    private final LreTestRunModel model;
    private final LreRestApis lreRestApis;
    private final LreAuthenticationManager authManager;

    public LreRunClient(LreTestRunModel model) {
        this.model = model;
        this.lreRestApis = new LreRestApis(model);
        this.authManager = new LreAuthenticationManager(lreRestApis, model);
        this.authManager.login();
    }

    public void startRun() {
        log.info("Starting run for test: {}", model.getTestToRun());

        try {
            // Execute the run workflow
            executeRunWorkflow();

            log.info("Run completed successfully for test: {}", model.getTestToRun());

        } catch (Exception e) {
            log.error("Run failed for test: {}", model.getTestToRun(), e);
            throw new LreException("Test run execution failed", e);
        }
    }

    private void executeRunWorkflow() {
        // Step 1: Fetch test details
        LreTestManager testManager = new LreTestManager(lreRestApis, model);
        testManager.fetchTestDetails();
        log.debug("Fetched test details, testId: {}", model.getTestId());

        // Step 2: Resolve test instance
        LreTestInstanceManager instanceManager = new LreTestInstanceManager(lreRestApis, model);
        instanceManager.resolveTestInstance();
        log.debug("Resolved test instance, testInstanceId: {}", model.getTestInstanceId());

        // Step 3: Check timeslot availability
        LreTimeslotManager timeslotManager = new LreTimeslotManager(lreRestApis, model);
        timeslotManager.checkTimeslotAvailableForTestId();
        log.debug("Timeslot availability confirmed");

        // Step 4: Execute the test run
        LreTestExecutor testExecutor = new LreTestExecutor(lreRestApis, model, timeslotManager);
        testExecutor.executeTestRun();
        log.info("Test run executed, runId: {}, dashboard: {}", model.getRunId(), model.getDashboardUrl());

        // Step 5: Monitor run status
        monitorRunCompletion(timeslotManager);
    }

    private void monitorRunCompletion(LreTimeslotManager timeslotManager) {
        int timeslotDuration = timeslotManager.getTotalMinutes();
        log.debug("Starting run monitoring with timeslot duration: {} minutes", timeslotDuration);

        LreRunStatusPoller runStatusPoller = new LreRunStatusPoller(lreRestApis, model, timeslotDuration);
        LreRunStatus runStatus = runStatusPoller.pollUntilDone();

        log.debug("Run monitoring completed. {}" , runStatus.getRunState());
    }

    @Override
    public void close() {
        try {
            authManager.close();
            log.debug("LreRunClient resources cleaned up");
        } catch (Exception e) {
            log.warn("Error during LreRunClient cleanup", e);
        }
    }
}