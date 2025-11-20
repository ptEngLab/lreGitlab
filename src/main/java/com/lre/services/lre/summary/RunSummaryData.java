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
//        String htmlContent = HtmlTemplateEngine.generateHtmlReport(runData);
        String[][] textSummary = generateTextSummary(model, runStatusExtended, thresholds);

        return new RunSummaryData("htmlContent", textSummary);
    }

    private static Map<String, String> prepareRunResultsData(LreTestRunModel model, LreRunStatusExtended runStatusExtended, ThresholdResult thresholds) {
        Map<String, String> runData = new HashMap<>();

        runData.put("RunID", String.valueOf(model.getRunId()));
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

        // Start with a wrapper and add more sophisticated styles for clarity and elegance
        html.append("<h3 style='color:#2c3e50; padding-bottom:2px; '>Transaction Summary</h3>").append("\n")
                .append("<table width='100%' cellpadding='8' cellspacing='0' border='0' style='border-collapse:collapse; font-size:12px; width:100%; background-color:#ffffff;'>").append("\n")
                // Header with purple gradient
                .append("<thead>").append("\n")
                .append("<tr style='background-color: #810ece; color: #fff; text-align: left; font-weight: 600; font-size:12px;'>").append("\n")
                .append("<th style='padding:15px; border-top:2px solid #6f2b8f; text-transform:uppercase;'>Name</th>").append("\n")
                .append("<th style='padding:15px; border-top:2px solid #6f2b8f; text-transform:uppercase;'># Passed</th>").append("\n")
                .append("<th style='padding:15px; border-top:2px solid #6f2b8f; text-transform:uppercase;'># Failed</th>").append("\n")
                .append("<th style='padding:15px; border-top:2px solid #6f2b8f; text-transform:uppercase;'># Stopped</th>").append("\n")
                .append("<th style='padding:15px; border-top:2px solid #6f2b8f; text-transform:uppercase;'>Success Rate %</th>").append("\n")
                .append("<th style='padding:15px; border-top:2px solid #6f2b8f; text-transform:uppercase;'>TPS</th>").append("\n")
                .append("</tr>").append("\n")
                .append("</thead>").append("\n")
                .append("<tbody>").append("\n");

        // Define total variables
        int totalPassed = 0, totalFailed = 0, totalStopped = 0;
        double totalTps = 0;
        boolean alternate = false;

        // Loop through each transaction metric
        for (LreTransactionMetrics txn : txns) {
            String rowBg = alternate ? "#f7f7f7" : "#ffffff"; // Zebra striping effect for rows
            alternate = !alternate;

            html.append("<tr style='background-color:").append(rowBg).append("; border-bottom:1px solid #ddd;'>").append("\n")
                    // Transaction Name
                    .append("<td style='padding:12px; font-weight:500;'>")
                    .append(txn.getTransactionName()).append("</td>").append("\n")
                    // Passed Count
                    .append("<td style='padding:12px; color:#28a745; font-weight:500;'>")
                    .append(txn.getPassedCount()).append("</td>").append("\n")
                    // Failed Count
                    .append("<td style='padding:12px; color:#e74c3c; font-weight:500;'>")
                    .append(txn.getFailedCount()).append("</td>").append("\n")
                    // Stopped Count
                    .append("<td style='padding:12px; color:#f39c12; font-weight:500;'>")
                    .append(txn.getStoppedCount()).append("</td>").append("\n")
                    // Success Rate %
                    .append("<td style='padding:12px; font-weight:500;'>")
                    .append(txn.getSuccessRatePercentage()).append("%</td>").append("\n")
                    // TPS (Transactions Per Second)
                    .append("<td style='padding:12px; font-weight:500;'>")
                    .append(txn.getTps()).append("</td>").append("\n")
                    .append("</tr>").append("\n");

            // Calculate totals
            totalPassed += txn.getPassedCount();
            totalFailed += txn.getFailedCount();
            totalStopped += txn.getStoppedCount();
            totalTps += txn.getTps();
        }

        // Totals row
        html.append("<tr style='font-weight:700; background-color:#ecf0f1; color:#333;'>").append("\n")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>Total</td>").append("\n")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalPassed).append("</td>").append("\n")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalFailed).append("</td>").append("\n")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalStopped).append("</td>").append("\n")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>")
                .append(totalPassed + totalFailed == 0 ? 0 : Math.round((double) totalPassed / (totalPassed + totalFailed) * 100))
                .append("</td>").append("\n")
                .append("<td style='padding:15px; border-top:2px solid #ccc;'>").append(totalTps).append("</td>").append("\n")
                .append("</tr>").append("\n")
                .append("</tbody></table>").append("\n");

        return html.toString();
    }

}