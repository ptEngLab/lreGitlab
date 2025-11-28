package com.lre.services.lre.summary.run;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.db.SqlQueries;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.transactions.LreTxnStats;
import com.lre.services.lre.report.TransactionStatsFetcher;
import com.lre.services.lre.summary.HtmlTemplateEngine;
import com.lre.services.lre.summary.ThresholdResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.lre.common.constants.ConfigConstants.RESULTS_DB_FORMAT;

public record RunSummaryData(String htmlContent, String[][] textSummary) {

    public static RunSummaryData createFrom(LreTestRunModel model, LreRunStatusExtended runStatusExtended) {
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatusExtended);

        // Prepare Run Data
        Map<String, String> runData = RunDataPreparer.prepare(model, runStatusExtended, thresholds);

        // Fetch transaction stats
        Path dbPath = model.getAnalysedReportPath().resolve(String.format(RESULTS_DB_FORMAT, model.getRunId()));
        List<LreTxnStats> txnStatsAll = TransactionStatsFetcher.fetch(dbPath);
        List<LreTxnStats> txnStatsTop5 = TransactionStatsFetcher.fetch(dbPath, SqlQueries.TXN_SUMMARY_SQL, null);

        // Generate HTML
        String txnHtml = TransactionHtmlBuilder.generateWithThreshold(txnStatsAll, txnStatsTop5);
        runData.put("TransactionTable", txnHtml);

        String htmlContent = HtmlTemplateEngine.generateHtmlReport(runData);
        String[][] textSummary = TextSummaryGenerator.generate(model, runStatusExtended, thresholds);

        return new RunSummaryData(htmlContent, textSummary);
    }
}




