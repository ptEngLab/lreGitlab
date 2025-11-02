package com.lre.services.lre.summary;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.transactions.LreTransactionMetrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RunSummaryData(String htmlContent, String[][] textSummary) {

    public static RunSummaryData createFrom(LreTestRunModel model, LreRunStatusExtended runStatusExtended,
                                            List<LreTransactionMetrics> txns) {
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatusExtended);
        Map<String, String> runData = prepareRunResultsData(model, runStatusExtended, thresholds);
        String transactionHtml = txns.isEmpty() ? "" : generateTransactionHtml(txns);
        runData.put("TransactionTable", transactionHtml);
        String htmlContent = HtmlTemplateEngine.generateHtmlReport(runData);
        String[][] textSummary = generateTextSummary(model, runStatusExtended, thresholds);

        return new RunSummaryData(htmlContent, textSummary);
    }

    private static Map<String, String> prepareRunResultsData(LreTestRunModel model, LreRunStatusExtended runStatusExtended, ThresholdResult thresholds) {
        Map<String, String> runData = new HashMap<>();

        runData.put("RunId", String.valueOf(model.getRunId()));
        runData.put("TestName", model.getTestName());
        runData.put("RunName", runStatusExtended.getName());
        runData.put("TransactionPassed", String.valueOf(runStatusExtended.getTransPassed()));
        runData.put("TransactionFailed", String.valueOf(runStatusExtended.getTransFailed()));
        runData.put("Vusers", String.valueOf(runStatusExtended.getVusersInvolved()));
        runData.put("TransactionPerSec", String.valueOf(runStatusExtended.getTransPerSec()));
        runData.put("HitsPerSec", String.valueOf(runStatusExtended.getHitsPerSec()));
        runData.put("AvgThroughput", String.valueOf(runStatusExtended.getThroughputAvg()));
        runData.put("Domain", model.getDomain());
        runData.put("Project", model.getProject());
        runData.put("TestID", String.valueOf(model.getTestId()));
        runData.put("TestFolder", model.getTestFolderPath());
        runData.put("TestInstanceID", String.valueOf(model.getTestInstanceId()));
        runData.put("Controller", runStatusExtended.getController());
        runData.put("StartTime", runStatusExtended.getStart().toString());
        runData.put("EndTime", runStatusExtended.getEnd().toString());
        runData.put("TestDuration", calculateTestDuration(runStatusExtended.getStart(), runStatusExtended.getEnd()));
        runData.put("Errors", String.valueOf(runStatusExtended.getErrors()));
        runData.put("ReportLink", model.getDashboardUrl());
        runData.put("RunID", String.valueOf(model.getTestInstanceId()));
        runData.put("LGsUsed", generateLgHtml(runStatusExtended.getLgs()));

        runData.put("RunStatus", runStatusExtended.getState());
        runData.put("RunResult", thresholds.runResult());
        runData.put("StatusBadgeColor", getStatusBadgeClass(thresholds.runResult()));

        return runData;
    }

    private static String generateLgHtml(String lgsData) {
        StringBuilder lgHtml = new StringBuilder();
        String[] lgList = lgsData.split(";");

        for (String lg : lgList) {
            String[] parts = lg.split("\\(");
            String name = parts[0].trim();
            String vusers = parts[1].replace(")", "").trim();

            lgHtml.append("<tr>" + "   <td bgcolor=\"#f8f9fa\" style=\"border-left:4px solid #9c27b0; padding:14px;\">"
                            + "       <div style=\"font-size:10px; color:#6c757d; font-weight:600; text-transform:uppercase;\">Load Generator</div>"
                            + "       <div style=\"font-size:13px; color:#9c27b0; font-weight:600; margin-top:6px;\">")
                    .append(name)
                    .append("</div>")
                    .append("   </td>")
                    .append("   <td width=\"2%\"></td>")
                    .append("   <td bgcolor=\"#f8f9fa\" style=\"border-left:4px solid #9c27b0; padding:14px;\">")
                    .append("       <div style=\"font-size:10px; color:#6c757d; font-weight:600; text-transform:uppercase;\">Vusers Allocated</div>")
                    .append("       <div style=\"font-size:13px; color:#9c27b0; font-weight:600; margin-top:6px;\">")
                    .append(vusers)
                    .append("</div>")
                    .append("   </td>")
                    .append("</tr>")
                    .append("<tr><td colspan=\"3\" height=\"8\"></td></tr>");
        }

        return lgHtml.toString();
    }

    private static String[][] generateTextSummary(LreTestRunModel model, LreRunStatusExtended runStatusExtended, ThresholdResult thresholds) {
        return new String[][]{
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
                        "Run Status: " + runStatusExtended.getState() + ", Result: " + thresholds.runResult()
                },
                {
                        "Start Time: " + runStatusExtended.getStart(),
                        "End Time: " + runStatusExtended.getEnd(),
                        "Test Duration: " + calculateTestDuration(runStatusExtended.getStart(), runStatusExtended.getEnd()),
                        "Vusers involved: " + runStatusExtended.getVusersInvolved()
                },
                {
                        "Transaction Passed: " + runStatusExtended.getTransPassed(),
                        "Transaction Failed: " + thresholds.failedTxnStr(),
                        "Errors: " + thresholds.errorStr(),
                        "Transaction per Sec: " + runStatusExtended.getTransPerSec()
                },
                {
                        "Hits per Sec: " + runStatusExtended.getHitsPerSec(),
                        "Throughput (avg): " + runStatusExtended.getThroughputAvg(),
                        "Controller used: " + runStatusExtended.getController(),
                        "LGs used: " + runStatusExtended.getLgs()
                },
        };
    }

    private static String calculateTestDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    private static String getStatusBadgeClass(String runResult) {
        if (runResult.contains("✅ PASSED")) return "#28a745";
        else if (runResult.contains("❌ FAILED")) return "#dc3545";
        else return "#ffc107";
    }

    private static String generateTransactionHtml(List<LreTransactionMetrics> txns) {
        StringBuilder html = new StringBuilder();

        // Start with a cleaner wrapper and modern fonts/colors
        html.append("<div style='font-family:Arial, sans-serif; color:#333; margin:20px;'>")
                .append("<h3 style='color:#2c3e50; border-bottom:2px solid #3498db; padding-bottom:6px;'>Transaction Summary</h3>")
                .append("<table width='100%' cellpadding='8' cellspacing='0' border='0' ")
                .append("style='border-collapse:collapse; font-size:14px; width:100%; box-shadow:0 4px 12px rgba(0, 0, 0, 0.1); border-radius:8px; overflow:hidden;'>")
                // Header
                .append("<thead>")
                .append("<tr style='background:linear-gradient(180deg, #4d055b, #3b7093); color:#fff; text-align:left;'>")
                .append("<th style='padding:15px; border:none; text-transform:uppercase; font-weight:600;'>Name</th>")
                .append("<th style='padding:15px; border:none; text-transform:uppercase; font-weight:600;'># Passed</th>")
                .append("<th style='padding:15px; border:none; text-transform:uppercase; font-weight:600;'># Failed</th>")
                .append("<th style='padding:15px; border:none; text-transform:uppercase; font-weight:600;'># Stopped</th>")
                .append("<th style='padding:15px; border:none; text-transform:uppercase; font-weight:600;'>Success Rate %</th>")
                .append("<th style='padding:15px; border:none; text-transform:uppercase; font-weight:600;'>TPS</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        // Define total variables
        int totalPassed = 0, totalFailed = 0, totalStopped = 0;
        double totalTps = 0;
        boolean alternate = false;

        // Loop through each transaction metric
        for (LreTransactionMetrics txn : txns) {
            String rowBg = alternate ? "#f9f9f9" : "#ffffff"; // Zebra striping
            alternate = !alternate;

            html.append("<tr style='background:").append(rowBg)
                    .append("; transition:background 0.3s, transform 0.3s;'>")
                    // Transaction Name
                    .append("<td style='padding:12px; border-bottom:1px solid #e1e1e1; font-weight:500;'>")
                    .append(txn.getTransactionName()).append("</td>")
                    // Passed Count
                    .append("<td style='padding:12px; border-bottom:1px solid #e1e1e1; color:#28a745; font-weight:500;'>")
                    .append(txn.getPassedCount()).append("</td>")
                    // Failed Count
                    .append("<td style='padding:12px; border-bottom:1px solid #e1e1e1; color:#e74c3c; font-weight:500;'>")
                    .append(txn.getFailedCount()).append("</td>")
                    // Stopped Count
                    .append("<td style='padding:12px; border-bottom:1px solid #e1e1e1; color:#f39c12; font-weight:500;'>")
                    .append(txn.getStoppedCount()).append("</td>")
                    // Success Rate %
                    .append("<td style='padding:12px; border-bottom:1px solid #e1e1e1; font-weight:500;'>")
                    .append(txn.getSuccessRatePercentage()).append("%</td>")
                    // TPS (Transactions Per Second)
                    .append("<td style='padding:12px; border-bottom:1px solid #e1e1e1; font-weight:500;'>")
                    .append(txn.getTps()).append("</td>")
                    .append("</tr>");

            // Calculate totals
            totalPassed += txn.getPassedCount();
            totalFailed += txn.getFailedCount();
            totalStopped += txn.getStoppedCount();
            totalTps += txn.getTps();
        }

        // Totals row
        html.append("<tr style='font-weight:700; background:#ecf0f1; color:#333;'>")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>Total</td>")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalPassed).append("</td>")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalFailed).append("</td>")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalStopped).append("</td>")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>")
                .append(totalPassed + totalFailed == 0 ? 0 :
                        Math.round((double) totalPassed / (totalPassed + totalFailed) * 100))
                .append("</td>")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalTps).append("</td>")
                .append("</tr>")
                .append("</tbody></table>");

        // Close wrapper div
        html.append("</div>");

        return html.toString();
    }

}