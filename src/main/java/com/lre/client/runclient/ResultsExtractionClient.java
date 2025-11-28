package com.lre.client.runclient;

import com.lre.client.base.BaseLreClient;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.utils.JsonUtils;
import com.lre.excel.ExcelDashboardWriter;
import com.lre.model.enums.RunState;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.run.LreRunStatusReqWeb;
import com.lre.services.lre.execution.LreTestManager;
import com.lre.services.lre.report.renderer.excel.ExcelReportPublisher;
import com.lre.services.lre.report.publisher.LreReportPublisher;
import com.lre.services.lre.report.renderer.excel.ExcelDataMapper;
import com.lre.services.lre.summary.RunSummaryData;
import com.lre.services.lre.summary.ThresholdResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.lre.common.constants.ConfigConstants.*;
import static com.lre.common.utils.CommonUtils.logTable;
import static com.lre.common.utils.CommonUtils.saveHtmlReport;

@Slf4j
public class ResultsExtractionClient extends BaseLreClient {

    private LreRunStatusExtended runStatus;

    public ResultsExtractionClient(LreTestRunModel model) {
        super(model);
    }

    public void fetchRunDetails() {
        trace("Fetching run status...");
        runStatus = fetchRunStatusExtended();

        new LreTestManager(model, lreRestApis).findTestById(runStatus.getTestId());

        RunState state = RunState.fromValue(runStatus.getState());
        log.info("Run details loaded: Run={}, Test={}, Name={}, State={}",
                model.getRunId(), model.getTestId(), model.getTestName(), state);
    }

    public void publishAnalysedReportIfFinished() {
        publishIfFinished(ANALYSED_RESULTS_TYPE, "Analysed");
    }

    public void publishHtmlReportIfFinished() {
        publishIfFinished(HTML_REPORTS_TYPE, "HTML");
    }

    public void createRunResultsForEMail() {
        log.info("Generating emailable report");
        RunSummaryData summary = RunSummaryData.createFrom(model, runStatus);
        String htmlReport = summary.htmlContent();
        Path reportPath = Paths.get(model.getWorkspace(), ARTIFACTS_DIR, EMAILABLE_HTML);
        saveHtmlReport(htmlReport, reportPath);
        log.info(logTable(summary.textSummary()));
    }

    public void extractRunReportsToExcel() throws IOException {
        ensureRunStatusFetched();

        Path analysedPath = model.getAnalysedReportPath();
        if (analysedPath == null) {
            throw new IllegalStateException(
                    "Analysed report path missing. " +
                            "Call publishAnalysedReportIfFinished() before extracting Excel reports."
            );
        }

        log.info("Generating Excel dashboard...");

        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatus);
        List<ExcelDashboardWriter.Section> sections =
                ExcelDataMapper.createSections(model, runStatus, thresholds);

        new ExcelReportPublisher(analysedPath, model.getRunId()).export(sections);

        log.info("Excel report exported successfully for Run {}", model.getRunId());
    }

    private void publishIfFinished(String type, String label) {
        ensureRunStatusFetched();
        RunState state = RunState.fromValue(runStatus.getState());

        if (state != RunState.FINISHED) {
            log.warn("Skipping {} report — Run {} is not finished (State={})", label, model.getRunId(), state);
            return;
        }

        trace("Publishing {} report...", label);
        LreReportPublisher publisher = new LreReportPublisher(lreRestApis, model);
        Optional<Path> path = publisher.publish(type);

        path.ifPresentOrElse(savedPath -> {
            if (ANALYSED_RESULTS_TYPE.equalsIgnoreCase(type)) model.setAnalysedReportAvailable(true);
            else if (HTML_REPORTS_TYPE.equalsIgnoreCase(type)) model.setHtmlReportAvailable(true);
            log.info("{} report saved successfully: {}", label, savedPath);
        }, () -> log.warn("{} report generation failed for Run {}", label, model.getRunId()));
    }

    private LreRunStatusExtended fetchRunStatusExtended() {
        LreRunStatusReqWeb req = LreRunStatusReqWeb.createRunStatusPayloadForRunId(model.getRunId());
        var results = lreRestApis.fetchRunResultsExtended(JsonUtils.toJson(req));

        if (results.isEmpty()) {
            throw new IllegalStateException("No run status available for Run " + model.getRunId());
        }

        return results.get(0);
    }

    private void ensureRunStatusFetched() {
        if (runStatus == null) {
            throw new IllegalStateException("Run status not initialized — call fetchRunDetails() first.");
        }
    }
}
