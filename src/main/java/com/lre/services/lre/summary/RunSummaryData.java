package com.lre.services.lre.summary;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreTxnStats;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.services.lre.report.fetcher.ReportDataService;
import com.lre.services.lre.report.preparer.RunDataPreparer;
import com.lre.services.lre.report.renderer.html.ErrorHtmlBuilder;
import com.lre.services.lre.report.renderer.html.HtmlTemplateEngine;
import com.lre.services.lre.report.renderer.html.TransactionHtmlBuilder;
import com.lre.services.lre.report.renderer.text.TextSummaryGenerator;

import java.util.List;
import java.util.Map;

public record RunSummaryData(String htmlContent, String[][] textSummary) {

    public static RunSummaryData createFrom(LreTestRunModel model, LreRunStatusExtended runStatusExtended) {
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatusExtended);
        Map<String, String> runData = RunDataPreparer.prepare(model, runStatusExtended, thresholds);

        ReportDataService.ReportData reportData =
                ReportDataService.fetchReportData(model.getAnalysedReportPath(), model.getRunId());

        List<LreTxnStats> txnStatsAll = reportData.transactions();
        List<LreErrorStats> errorStats = reportData.errors();

        List<LreTxnStats> topSlowestTxns = ReportDataService.getTopSlowestTransactions(txnStatsAll);
        List<LreErrorStats> topErrors = ReportDataService.getTopErrors(errorStats);

        String txnHtml = TransactionHtmlBuilder.generateWithThreshold(txnStatsAll, topSlowestTxns);
        String errorsHtml = ErrorHtmlBuilder.generateWithThreshold(errorStats, topErrors);

        runData.put("TransactionTable", txnHtml);
        runData.put("ErrorsTable", errorsHtml);

        String htmlContent = HtmlTemplateEngine.generateHtmlReport(runData);
        String[][] textSummary = TextSummaryGenerator.generate(model, runStatusExtended, thresholds);

        return new RunSummaryData(htmlContent, textSummary);
    }

}
