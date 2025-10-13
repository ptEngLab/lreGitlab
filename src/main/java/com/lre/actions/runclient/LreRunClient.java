package com.lre.actions.runclient;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.model.enums.RunState;
import com.lre.model.run.LreRunStatus;
import com.lre.services.*;
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
        try {
            LreRunStatus finalStatus = executeRunWorkflow();
            if (isRunFailure(finalStatus)) {
                String errorMessage = String.format("Run failed for test: %s. Final state: %s", model.getTestToRun(), finalStatus.getRunState());
                log.error(errorMessage);
                throw new LreException(errorMessage);
            }
            log.info("Run completed successfully for test: {}", model.getTestToRun());
        } catch (Exception e) {

            log.error("Run failed for test: {}", model.getTestToRun(), e);
            throw new LreException("Test run execution failed", e);
        }
    }

    private LreRunStatus executeRunWorkflow() {
        fetchTestDetails();
        resolveTestInstance();
        LreTimeslotManager timeslotManager = checkTimeslotAvailability();
        initiateTestRun(timeslotManager);
        return monitorRunCompletion(timeslotManager);
    }

    private void fetchTestDetails() {
        LreTestManager testManager = new LreTestManager(model, lreRestApis);
        testManager.fetchTestDetails();
        log.debug("Fetched test details, testId: {}", model.getTestId());
    }

    private void resolveTestInstance() {
        LreTestInstanceManager instanceManager = new LreTestInstanceManager(lreRestApis, model);
        instanceManager.resolveTestInstance();
        log.debug("Resolved test instance, testInstanceId: {}", model.getTestInstanceId());
    }

    private LreTimeslotManager checkTimeslotAvailability() {
        LreTimeslotManager timeslotManager = new LreTimeslotManager(lreRestApis, model);
        timeslotManager.checkTimeslotAvailableForTestId();
        log.debug("Timeslot availability confirmed");
        return timeslotManager;
    }

    private void initiateTestRun(LreTimeslotManager timeslotManager) {
        LreTestExecutor testExecutor = new LreTestExecutor(lreRestApis, model, timeslotManager);
        testExecutor.executeTestRun();
        log.info("Run started successfully. Run ID: {}, Dashboard URL: {}", model.getRunId(), model.getDashboardUrl());
    }

    private LreRunStatus monitorRunCompletion(LreTimeslotManager timeslotManager) {
        int timeslotDuration = timeslotManager.getTotalMinutes();
        log.debug("Starting run monitoring with timeslot duration: {} minutes", timeslotDuration);
        LreRunStatusPoller runStatusPoller = new LreRunStatusPoller(lreRestApis, model, timeslotDuration);
        LreRunStatus runStatus = runStatusPoller.pollUntilDone();
        log.debug("Run monitoring completed. {}", runStatus.getRunState());
        return runStatus;

    }

    private boolean isRunFailure(LreRunStatus status) {
        if (status == null) return true;
        RunState runState = RunState.fromValue(status.getRunState());
        return runState == RunState.RUN_FAILURE || runState == RunState.UNDEFINED;
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
