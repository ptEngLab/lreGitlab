package com.lre.client.runclient;

import com.lre.client.base.BaseLreClient;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.model.run.LreRunStatus;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.services.lre.execution.LreTestExecutor;
import com.lre.services.lre.execution.LreTestInstanceManager;
import com.lre.services.lre.execution.LreTestManager;
import com.lre.services.lre.execution.LreTimeslotManager;
import com.lre.services.lre.monitor.RunStatusMonitor;
import com.lre.services.lre.poller.LreRunStatusPoller;
import com.lre.services.lre.report.fetcher.ReportDataService;
import com.lre.services.lre.summary.RunSummaryData;
import lombok.extern.slf4j.Slf4j;

import static com.lre.common.utils.CommonUtils.logTable;

@Slf4j
public class LreRunClient extends BaseLreClient {

    public LreRunClient(LreTestRunModel model) {
        super(model);
    }

    public void startRun() {
        try {
            LreRunStatus finalStatus = executeRunWorkflow();
            if (model.isTestFailed()) {
                printRunSummary();
                throw new LreException(getErrorMsg(finalStatus));
            }
            log.info("Run completed successfully for test: {}", model.getTestToRun());

        } catch (LreException le) {
            log.error("LRE execution failed: {}", le.getMessage());
            throw le;
        } catch (Exception ex) {
            log.error("Unexpected error during test run [{}]: {}", model.getTestToRun(), ex.getMessage(), ex);
            throw new LreException("Unexpected failure during test execution", ex);
        }
    }

    private String getErrorMsg(LreRunStatus finalStatus) {
        String reason = model.getFailureReason() != null ? model.getFailureReason() : "Unknown failure";
        return String.format(
                "Test failed for [%s]: %s (Last state: %s, Errors: %d, FailedTxns: %d)",
                model.getTestToRun(),
                reason,
                finalStatus.getRunState(),
                finalStatus.getTotalErrors(),
                finalStatus.getTotalFailedTransactions()
        );
    }

    public void printRunSummary() {
        RunStatusMonitor statusMonitor = new RunStatusMonitor(lreRestApis, model);
        LreRunStatusExtended runStatus = statusMonitor.fetchRunStatusExtended();
        var reportData = ReportDataService.fetchReportData(model.getAnalysedReportPath(), model.getRunId());
        RunSummaryData summary = RunSummaryData.createFrom(model, runStatus, reportData);
        log.info(logTable(summary.textSummary()));
    }



    private LreRunStatus executeRunWorkflow() {
        fetchTestDetails();
        resolveTestInstance();
        LreTimeslotManager timeslotManager = checkTimeslotAvailability();
        initiateTestRun(timeslotManager);
        return monitorRunCompletion(timeslotManager);
    }

    private void fetchTestDetails() {
        new LreTestManager(model, lreRestApis).fetchTestDetails();
    }

    private void resolveTestInstance() {
        new LreTestInstanceManager(lreRestApis, model).resolveTestInstance();
    }

    private LreTimeslotManager checkTimeslotAvailability() {
        LreTimeslotManager timeslotManager = new LreTimeslotManager(lreRestApis, model);
        timeslotManager.checkTimeslotAvailableForTestId();
        return timeslotManager;
    }

    private void initiateTestRun(LreTimeslotManager timeslotManager) {
        new LreTestExecutor(lreRestApis, model, timeslotManager).executeTestRun();
        log.info("Run started. Run ID: {}, Dashboard: {}", model.getRunId(), model.getDashboardUrl());
    }

    private LreRunStatus monitorRunCompletion(LreTimeslotManager timeslotManager) {
        int duration = timeslotManager.getTotalMinutes();
        LreRunStatusPoller poller = new LreRunStatusPoller(lreRestApis, model, duration);
        return poller.pollUntilDone();
    }

}
