package com.lre.services.lre.summary;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.report.LreErrorStats;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.report.LreTxnStats;
import com.lre.services.lre.report.fetcher.ReportStatsFetcher;
import com.lre.services.lre.report.preparer.RunDataPreparer;
import com.lre.services.lre.report.renderer.html.HtmlTemplateEngine;
import com.lre.services.lre.report.renderer.html.TransactionHtmlBuilder;
import com.lre.services.lre.report.renderer.text.TextSummaryGenerator;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.lre.common.constants.ConfigConstants.ERRORS_DB_FORMAT;
import static com.lre.common.constants.ConfigConstants.RESULTS_DB_FORMAT;

public record RunSummaryData(String htmlContent, String[][] textSummary) {

    private static final int TOP_N = 5;

    public static RunSummaryData createFrom(LreTestRunModel model, LreRunStatusExtended runStatusExtended) {
        // 1. Threshold evaluation
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatusExtended);

        // 2. Prepare Run Data
        Map<String, String> runData = RunDataPreparer.prepare(model, runStatusExtended, thresholds);

        // 3. Fetch ALL transaction stats once
        Path resultsDbPath = model.getAnalysedReportPath().resolve(String.format(RESULTS_DB_FORMAT, model.getRunId()));
        Path errorsDbPath = model.getAnalysedReportPath().resolve(ERRORS_DB_FORMAT);

        List<LreTxnStats> txnStatsAll = ReportStatsFetcher.fetchTransactions(resultsDbPath);
        List<LreErrorStats> errorStats = ReportStatsFetcher.fetchErrors(errorsDbPath);

        // 4. Compute top 5 slowest transactions by p95 in-memory
        List<LreTxnStats> top5SlowestTxns = getTop5Slowest(txnStatsAll);
        List<LreErrorStats> top5Errors = getTop5Errors(errorStats);

        // 5. Generate HTML for transaction table
        String txnHtml = TransactionHtmlBuilder.generateWithThreshold(txnStatsAll, top5SlowestTxns);
        runData.put("TransactionTable", txnHtml);

        // 6. Render HTML and text summary
        String htmlContent = HtmlTemplateEngine.generateHtmlReport(runData);
        String[][] textSummary = TextSummaryGenerator.generate(model, runStatusExtended, thresholds);

        return new RunSummaryData(htmlContent, textSummary);
    }


    /**
     * Returns at most the top 5 slowest transactions based on p95.
     */
    private static List<LreTxnStats> getTop5Slowest(List<LreTxnStats> allStats) {
        return allStats.stream()
                .sorted(Comparator.comparing(LreTxnStats::getP95).reversed())
                .limit(TOP_N)
                .toList();
    }

    private static List<LreErrorStats> getTop5Errors(List<LreErrorStats> allStats) {
        return allStats.stream()
                .limit(TOP_N)
                .toList();
    }
}
