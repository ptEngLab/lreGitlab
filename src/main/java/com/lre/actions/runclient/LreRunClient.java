package com.lre.actions.runclient;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import com.lre.model.run.LreRunStatus;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.run.LreRunStatusReqWeb;
import com.lre.services.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.lre.actions.utils.CommonUtils.logTableDynamic;

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
            if (model.isTestFailed()) throw new LreException(getErrorMsg(finalStatus));
            log.info("Run completed successfully for test: {}", model.getTestToRun());
        } catch (LreException le) {
            log.debug("LRE execution failed: {}", le.getMessage());
            throw le;
        } catch (Exception ex) {
            log.error("Unexpected error during test run [{}]: {}", model.getTestToRun(), ex.getMessage());
            throw new LreException("Unexpected failure during test execution", ex);
        }
    }

    private String getErrorMsg(LreRunStatus finalStatus) {
        String reason = model.getFailureReason() != null ? model.getFailureReason() : "Unknown failure";
        return String.format(
                "Test failed for [%s]: %s (Last known state: %s, Errors: %d, FailedTxns: %d)",
                model.getTestToRun(),
                reason,
                finalStatus.getRunState(),
                finalStatus.getTotalErrors(),
                finalStatus.getTotalFailedTransactions()
        );
    }


    public void publishRunReport() {
        if (model.isHtmlReportAvailable()) {
            LreReportPublisher publisher = new LreReportPublisher(lreRestApis, model);
            Path reportPath = publisher.publish();
            if (reportPath != null) log.info("Report successfully published at: {}", reportPath.resolve("index.html"));
            else log.warn("Report not available for run id: {}", model.getRunId());
        }
    }

    public void printRunSummary() {
        LreRunStatusReqWeb runStatusReq = LreRunStatusReqWeb.createRunStatusPayloadForRunId(model.getRunId());
        LreRunStatusExtended runStatusExtended = lreRestApis
                .fetchRunResultsExtended(JsonUtils.toJson(runStatusReq))
                .get(0);

        // Determine if thresholds were exceeded
        long errorThreshold = model.getMaxErrors();
        long failedTxnThreshold = model.getMaxFailedTxns();

        boolean errorExceeded = runStatusExtended.getErrors() >= errorThreshold;
        boolean failedTxnExceeded = runStatusExtended.getTransFailed() >= failedTxnThreshold;

        String errorStr = errorExceeded
                ? runStatusExtended.getErrors() + " ⚠ (limit: " + errorThreshold + ") — Exceeded"
                : runStatusExtended.getErrors() + " (limit: " + errorThreshold + ")";

        String failedTxnStr = failedTxnExceeded
                ? runStatusExtended.getTransFailed() + " ⚠ (limit: " + failedTxnThreshold + ") — Exceeded"
                : runStatusExtended.getTransFailed() + " (limit: " + failedTxnThreshold + ")";

        String runResult = (errorExceeded || failedTxnExceeded || model.isTestFailed())
                ? "❌ FAILED"
                : "✅ PASSED";

        String[][] rows = {
                {
                        "Domain: " + model.getDomain(),
                        "Project: " + model.getProject(),
                        "Test Name: " + model.getTestName(),
                        "Test Id: " + model.getTestId()
                },
                {
                        "Test Folder: " + model.getTestFolderPath(),
                        "Test Instance Id: " + model.getTestInstanceId(),
                        "Run Name: " + runStatusExtended.getName(),
                        "Run Status: " + runStatusExtended.getState() + ", Result: " + runResult
                },
                {
                        "Start Time: " + runStatusExtended.getStart(),
                        "End Time: " + runStatusExtended.getEnd(),
                        "Test Duration: " + calculateTestDuration(runStatusExtended.getStart(), runStatusExtended.getEnd()),
                        "Vusers involved: " + runStatusExtended.getVusersInvolved()
                },
                {
                        "Transaction Passed: " + runStatusExtended.getTransPassed(),
                        "Transaction Failed: " + failedTxnStr,
                        "Errors: " + errorStr,
                        "Transaction per Sec: " + runStatusExtended.getTransPerSec()
                },
                {
                        "Hits per Sec: " + runStatusExtended.getHitsPerSec(),
                        "Throughput (avg): " + runStatusExtended.getThroughputAvg(),
                        "Controller used: " + runStatusExtended.getController(),
                        "LGs used: " + runStatusExtended.getLgs()
                },
        };

        log.info(logTableDynamic(rows));
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

    private static String calculateTestDuration(LocalDateTime start, LocalDateTime end) {

        Duration duration = (start == null || end == null) ? Duration.ZERO : Duration.between(start, end);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
