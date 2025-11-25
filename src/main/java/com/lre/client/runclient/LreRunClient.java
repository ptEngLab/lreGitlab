package com.lre.client.runclient;

import com.lre.client.base.BaseLreClient;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.JsonUtils;
import com.lre.model.run.LreRunStatus;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.run.LreRunStatusReqWeb;
import com.lre.model.transactions.LreTransactionMetrics;
import com.lre.services.lre.LreTestExecutor;
import com.lre.services.lre.LreTestInstanceManager;
import com.lre.services.lre.LreTestManager;
import com.lre.services.lre.LreTimeslotManager;
import com.lre.services.lre.poller.LreRunStatusPoller;
import com.lre.services.lre.summary.LreTxnSummaryFetcher;
import com.lre.services.lre.summary.RunSummaryData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.ARTIFACTS_DIR;
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
        LreRunStatusExtended runStatus = fetchRunStatusExtended();
        List<LreTransactionMetrics> txns = new LreTxnSummaryFetcher(lreRestApis, model).fetchTransactionSummary();
        String[][] summaryRows = prepareRunSummary(runStatus, txns);
        log.info(logTable(summaryRows));
    }

    private LreRunStatusExtended fetchRunStatusExtended() {
        LreRunStatusReqWeb req = LreRunStatusReqWeb.createRunStatusPayloadForRunId(model.getRunId());
        List<LreRunStatusExtended> results = lreRestApis.fetchRunResultsExtended(JsonUtils.toJson(req));
        if (results.isEmpty()) throw new LreException("No run status found for Run ID " + model.getRunId());
        return results.get(0);
    }

    private String[][] prepareRunSummary(LreRunStatusExtended runStatusExtended, List<LreTransactionMetrics> txns) {
        RunSummaryData summary = RunSummaryData.createFrom(model, runStatusExtended, txns);
        String htmlReport = summary.htmlContent();
        Path reportPath = Paths.get(model.getWorkspace(), ARTIFACTS_DIR, "LreReports", "email.html");
        saveHtmlReport(htmlReport, reportPath);
        return summary.textSummary();
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

    private void saveHtmlReport(String htmlContent, Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, htmlContent);
        } catch (IOException e) {
            throw new LreException("Failed to save HTML report: " + filePath, e);
        }
    }
}
