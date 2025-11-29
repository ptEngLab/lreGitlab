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

    public static RunSummaryData createFrom(LreTestRunModel model,
                                            LreRunStatusExtended runStatusExtended,
                                            ReportDataService.ReportData reportData) {
        // Threshold evaluation
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatusExtended);

        // Prepare run data
        Map<String, String> runData = RunDataPreparer.prepare(model, runStatusExtended, thresholds);

        // Use pre-fetched stats
        List<LreTxnStats> txnStatsAll = reportData.transactions();
        List<LreErrorStats> errorStats = reportData.errors();

        // Compute top N
        List<LreTxnStats> topSlowestTxns = ReportDataService.getTopSlowestTransactions(txnStatsAll);
        List<LreErrorStats> topErrors = ReportDataService.getTopErrors(errorStats);

        // Transaction HTML
        String txnHtml = TransactionHtmlBuilder.generateWithThreshold(txnStatsAll, topSlowestTxns);
        runData.put("TransactionTable", txnHtml);

        // Error HTML
        String errorHtml = ErrorHtmlBuilder.generateWithThreshold(errorStats, topErrors);
        runData.put("ErrorTable", errorHtml);

        // Render HTML and text summary
        String htmlContent = HtmlTemplateEngine.generateHtmlReport(runData);
        String[][] textSummary = TextSummaryGenerator.generate(model, runStatusExtended, thresholds);

        return new RunSummaryData(htmlContent, textSummary);
    }
}
