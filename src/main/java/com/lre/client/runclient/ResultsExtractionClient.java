package com.lre.client.runclient;

import com.lre.client.base.BaseLreClient;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.excel.ExcelDashboardWriter;
import com.lre.model.enums.RunState;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.test.Test;
import com.lre.services.lre.calculation.SteadyStateCalculator;
import com.lre.services.lre.calculation.SteadyStateResult;
import com.lre.services.lre.execution.LreTestManager;
import com.lre.services.lre.monitor.RunStatusMonitor;
import com.lre.services.lre.report.fetcher.ReportDataService;
import com.lre.services.lre.report.publisher.LreReportPublisher;
import com.lre.services.lre.report.renderer.excel.ExcelDataMapper;
import com.lre.services.lre.report.renderer.excel.ExcelReportPublisher;
import com.lre.services.lre.summary.RunSummaryData;
import com.lre.services.lre.summary.ThresholdResult;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.lre.common.constants.ConfigConstants.*;
import static com.lre.common.utils.CommonUtils.logTable;
import static com.lre.common.utils.CommonUtils.saveHtmlReport;

/**
 * Client responsible for extracting and publishing run results.
 */
@Slf4j
public class ResultsExtractionClient extends BaseLreClient {

    private LreRunStatusExtended runStatus;
    private ReportDataService.ReportData reportData;

    private final LreTestManager testManager;
    private final LreReportPublisher reportPublisher;

    private Test test;


    public ResultsExtractionClient(LreTestRunModel model) {
        super(model);
        this.testManager = new LreTestManager(model, lreRestApis);
        this.reportPublisher = new LreReportPublisher(lreRestApis, model);
    }


    public void fetchRunDetails() {
        log.debug("Fetching run status for Run {}", model.getRunId());
        runStatus = fetchRunStatusExtended();
        test = testManager.findTestById(runStatus.getTestId());
        RunState state = RunState.fromValue(runStatus.getState());
        log.info("Run details loaded: Run={}, Test={}, Name={}, State={}", model.getRunId(), model.getTestId(), model.getTestName(), state);
    }

    public void publishAnalysedReportIfFinished() {
        publishReportIfFinished(ANALYSED_RESULTS_TYPE, ReportType.ANALYSED);
    }

    private void calculateSteadyStateTimings() {
        SteadyStateCalculator steadyStateCalculator = new SteadyStateCalculator(test);
        List<SteadyStateResult> results = steadyStateCalculator.calculateSteadyState(123456);
        log.info("Steady state starts at: {}s", results.get(0).getSteadyStateDurationSeconds());
    }

    public void publishHtmlReportIfFinished() {
        publishReportIfFinished(HTML_REPORTS_TYPE, ReportType.HTML);
    }

    public void createRunResultsForEmail() {
        ensureReportDataFetched();
        log.info("Generating emailable report for Run {}", model.getRunId());
        RunSummaryData summary = RunSummaryData.createFrom(model, runStatus, reportData);
        Path reportPath = Paths.get(model.getWorkspace(), ARTIFACTS_DIR, EMAILABLE_HTML);
        saveHtmlReport(summary.htmlContent(), reportPath);
        log.info("Emailable report created at {}", reportPath);
        log.info(logTable(summary.textSummary()));
    }

    public void extractRunReportsToExcel() {
        ensureReportDataFetched();
        log.info("Generating Excel dashboard for Run {}", model.getRunId());
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatus);
        List<ExcelDashboardWriter.Section> sections = ExcelDataMapper.createSections(model, runStatus, thresholds);
        new ExcelReportPublisher(model.getRunId()).export(sections, reportData);
        log.info("Excel report exported successfully for Run {}", model.getRunId());
    }

    private void publishReportIfFinished(String type, ReportType reportType) {
        RunState state = RunState.fromValue(runStatus.getState());
        if (state != RunState.FINISHED) {
            log.warn("Skipping {} report — Run {} is not finished (State={})", reportType.label, model.getRunId(), state);
            return;
        }
        log.info("Publishing {} report for Run {}", reportType.label, model.getRunId());
        Optional<Path> path = reportPublisher.publish(type);
        path.ifPresentOrElse(savedPath -> {
            updateModelAvailability(reportType);
            log.info("{} report saved successfully: {}", reportType.label, savedPath);
        }, () -> log.error("{} report generation failed for Run {}", reportType.label, model.getRunId()));
    }

    private void updateModelAvailability(ReportType reportType) {
        switch (reportType) {
            case ANALYSED -> model.setAnalysedReportAvailable(true);
            case HTML -> model.setHtmlReportAvailable(true);
        }
    }

    private LreRunStatusExtended fetchRunStatusExtended() {
        RunStatusMonitor statusMonitor = new RunStatusMonitor(lreRestApis, model);
        return statusMonitor.fetchRunStatusExtended();
    }

    private void ensureReportDataFetched() {
        if (reportData == null) {
            Path analysedPath = model.getAnalysedReportPath();
            if (analysedPath == null) {
                String msg = String.format("Analysed report path missing for Run %d. Call publishAnalysedReportIfFinished() first.", model.getRunId());
                throw new LreException(msg);
            }
            log.info("Fetching report data from DB for Run {}", model.getRunId());
            reportData = ReportDataService.fetchReportData(analysedPath, model.getRunId());
        } else {
            log.debug("Reusing cached report data for Run {}", model.getRunId());
        }
    }

    private enum ReportType {
        ANALYSED("Analysed"),
        HTML("HTML");
        private final String label;
        ReportType(String label) {
            this.label = label;
        }
    }
}
