package com.lre.services.lre.summary;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatusExtended;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public record RunSummaryData(String htmlContent, String[][] textSummary) {

    public static RunSummaryData createFrom(LreTestRunModel model, LreRunStatusExtended runStatusExtended) {
        ThresholdResult thresholds = ThresholdResult.checkThresholds(model, runStatusExtended);
        Map<String, String> testData = prepareTestData(model, runStatusExtended, thresholds);
        String htmlContent = HtmlTemplateEngine.generateHtmlReport(testData);
        String[][] textSummary = generateTextSummary(model, runStatusExtended, thresholds);

        return new RunSummaryData(htmlContent, textSummary);
    }

    private static Map<String, String> prepareTestData(LreTestRunModel model, LreRunStatusExtended runStatusExtended, ThresholdResult thresholds) {
        Map<String, String> testData = new HashMap<>();

        testData.put("RunId", String.valueOf(model.getRunId()));
        testData.put("TestName", model.getTestName());
        testData.put("RunName", runStatusExtended.getName());
        testData.put("TransactionPassed", String.valueOf(runStatusExtended.getTransPassed()));
        testData.put("TransactionFailed", String.valueOf(runStatusExtended.getTransFailed()));
        testData.put("Vusers", String.valueOf(runStatusExtended.getVusersInvolved()));
        testData.put("TransactionPerSec", String.valueOf(runStatusExtended.getTransPerSec()));
        testData.put("HitsPerSec", String.valueOf(runStatusExtended.getHitsPerSec()));
        testData.put("AvgThroughput", String.valueOf(runStatusExtended.getThroughputAvg()));
        testData.put("Domain", model.getDomain());
        testData.put("Project", model.getProject());
        testData.put("TestID", String.valueOf(model.getTestId()));
        testData.put("TestFolder", model.getTestFolderPath());
        testData.put("TestInstanceID", String.valueOf(model.getTestInstanceId()));
        testData.put("Controller", runStatusExtended.getController());
        testData.put("StartTime", runStatusExtended.getStart().toString());
        testData.put("EndTime", runStatusExtended.getEnd().toString());
        testData.put("TestDuration", calculateTestDuration(runStatusExtended.getStart(), runStatusExtended.getEnd()));
        testData.put("Errors", String.valueOf(runStatusExtended.getErrors()));
        testData.put("ReportLink", model.getDashboardUrl());
        testData.put("RunID", String.valueOf(model.getTestInstanceId()));
        testData.put("LGsUsed", generateLgHtml(runStatusExtended.getLgs()));

        testData.put("RunStatus", runStatusExtended.getState());
        testData.put("RunResult", thresholds.runResult());
        testData.put("StatusBadgeClass", getStatusBadgeClass(thresholds.runResult()));

        return testData;
    }

    private static String generateLgHtml(String lgsData) {
        StringBuilder lgHtml = new StringBuilder();
        String[] lgList = lgsData.split(";");

        for (String lg : lgList) {
            String[] parts = lg.split("\\(");
            String name = parts[0].trim();
            String vusers = parts[1].replace(")", "").trim() + " Vusers";

            lgHtml.append("<div class=\"lg-chip\">")
                    .append("<span class=\"lg-name\">").append(name).append("</span>")
                    .append("<span class=\"vuser-badge\">").append(vusers).append("</span>")
                    .append("</div>\n");
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
        if (runResult.contains("✅ PASSED")) return "status-passed";
        else if (runResult.contains("❌ FAILED")) return "status-failed";
        else return "status-warning";
    }
}