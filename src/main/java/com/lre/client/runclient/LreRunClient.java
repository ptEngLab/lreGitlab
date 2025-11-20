package com.lre.client.runclient;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.constants.ConfigConstants;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.JsonUtils;
import com.lre.db.SQLiteConnectionManager;
import com.lre.db.SqlQueries;
import com.lre.model.run.LreRunStatus;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.run.LreRunStatusReqWeb;
import com.lre.model.transactions.LreTransactionMetrics;
import com.lre.services.lre.LreTestExecutor;
import com.lre.services.lre.LreTestInstanceManager;
import com.lre.services.lre.LreTestManager;
import com.lre.services.lre.LreTimeslotManager;
import com.lre.services.lre.auth.LreAuthenticationManager;
import com.lre.services.lre.poller.LreRunStatusPoller;
import com.lre.services.lre.report.LreReportPublisher;
import com.lre.services.lre.summary.LreTxnSummaryFetcher;
import com.lre.services.lre.summary.RunSummaryData;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.lre.common.constants.ConfigConstants.*;
import static com.lre.common.utils.CommonUtils.logTable;

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
            if (model.isTestFailed()) {
                printRunSummary();
                throw new LreException(getErrorMsg(finalStatus));
            }
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
        if (!model.isHtmlReportAvailable()) {
            log.warn("HTML report not available for run id: {} (Test: {})", model.getRunId(), model.getTestToRun());
            return;
        }
        LreReportPublisher publisher = new LreReportPublisher(lreRestApis, model);
        Optional<Path> reportPath = publisher.publish(HTML_REPORTS_TYPE);
        if (reportPath.isPresent()) log.info("HTML Report extracted: {}", reportPath.get());
        else log.warn("HTML Report extraction failed for run id: {}", model.getRunId());

    }

    public void publishAnalysedReports() {
        if (!model.isAnalysedReportAvailable()) {
            log.warn("Analysed report not available for run id: {} (Test: {})", model.getRunId(), model.getTestToRun());
            return;
        }
        LreReportPublisher publisher = new LreReportPublisher(lreRestApis, model);
        Optional<Path> reportPath = publisher.publish(ANALYSED_RESULTS_TYPE);
        if (reportPath.isPresent()) log.info("Analysed Report extracted: {}", reportPath.get());
        else log.warn("Analysed Report extraction failed for run id: {}", model.getRunId());
    }

    public void extractRunReportsFromDb() throws IOException {
        String dbPath = model.getAnalysedReportPath() + "/Results_" + model.getRunId() + ".db";

        // Verify database exists
        if (!Files.exists(Paths.get(dbPath))) {
            log.error("Database file not found: {}", dbPath);
            throw new FileNotFoundException("Database file not found: " + dbPath);
        }

        SQLiteConnectionManager dbManager = new SQLiteConnectionManager(dbPath);

        Path excelFilePath = Paths.get(ConfigConstants.DEFAULT_OUTPUT_DIR, ARTIFACTS_DIR, "output/results.xlsx");

        try {
            // Add progress logging
            log.info("Starting database export from: {}", dbPath);
            log.debug("Output Excel file: {}", excelFilePath);

            dbManager.exportToExcelV2WithMerging(SqlQueries.TXN_SUMMARY_SQL, excelFilePath.toString(), "GenreName");

            log.debug("Successfully exported results to: {}", excelFilePath);

        } catch (Exception e) {
            log.error("Failed to extract run reports from database: {}", dbPath, e);
            throw new IOException("Database export failed", e);
        }
    }


    public void printRunSummary() {
        LreRunStatusExtended runStatus = fetchRunStatusExtended();
        List<LreTransactionMetrics> txns = new LreTxnSummaryFetcher(lreRestApis, model).fetchTransactionSummary();
        String[][] summaryRows = prepareRunSummary(runStatus, txns);
        log.info(logTable(summaryRows));
    }

    private LreRunStatusExtended fetchRunStatusExtended() {
        LreRunStatusReqWeb runStatusReq = LreRunStatusReqWeb.createRunStatusPayloadForRunId(model.getRunId());
        return lreRestApis.fetchRunResultsExtended(JsonUtils.toJson(runStatusReq)).get(0);
    }

    private String[][] prepareRunSummary(LreRunStatusExtended runStatusExtended, List<LreTransactionMetrics> txns) {
        RunSummaryData summary = RunSummaryData.createFrom(model, runStatusExtended, txns);
        String htmlReport = summary.htmlContent();
        Path reportDir = Paths.get(model.getWorkspace(), ARTIFACTS_DIR, "LreReports/email.html");
        saveHtmlReport(htmlReport, reportDir.toString());
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

    private void saveHtmlReport(String htmlContent, String filePath) {
        try {
            Path path = Paths.get(filePath);

            // Create parent directories if they don't exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            Files.writeString(path, htmlContent);

        } catch (IOException e) {
            log.error("Failed to save HTML report to: {}", filePath, e); // More specific logging
            throw new RuntimeException("Failed to save HTML report to: " + filePath, e);
        }
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
