package com.lre.services.lre.report.preparer;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.services.lre.report.renderer.html.LgHtmlBuilder;
import com.lre.services.lre.summary.ThresholdResult;

import java.util.Map;

import static com.lre.common.constants.ConfigConstants.DASHBOARD_URL;
import static com.lre.common.utils.CommonUtils.calculateTestDuration;
import static com.lre.common.utils.CommonUtils.formatDateTime;

public class RunDataPreparer {
    public static Map<String, String> prepare(LreTestRunModel model, LreRunStatusExtended runStatusExtended, ThresholdResult thresholds) {
        var runData = new java.util.HashMap<String, String>();

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
        runData.put("TestID", String.valueOf(runStatusExtended.getTestId()));
        runData.put("TestFolder", model.getTestFolderPath());
        runData.put("TestInstanceID", String.valueOf(runStatusExtended.getTestInstanceId()));
        runData.put("Controller", runStatusExtended.getController());
        runData.put("StartTime", formatDateTime(runStatusExtended.getStart()));
        runData.put("EndTime", formatDateTime(runStatusExtended.getEnd()));
        runData.put("TestDuration", calculateTestDuration(runStatusExtended.getStart(), runStatusExtended.getEnd()));
        runData.put("Errors", String.valueOf(runStatusExtended.getErrors()));

        String dashboardUrl = String.format(DASHBOARD_URL, model.getLreServerUrl(), model.getRunId());
        runData.put("ReportLink", dashboardUrl);

        runData.put("LGsUsed", LgHtmlBuilder.generate(runStatusExtended.getLgs()));

        runData.put("RunStatus", runStatusExtended.getState());
        runData.put("RunResult", thresholds.runResult());
        runData.put("StatusBadgeColor", RunDataPreparer.getStatusBadgeColor(thresholds.runResult()));
        runData.put("FailureReason", generateFailureReason(model, runStatusExtended, thresholds));

        return runData;
    }

    private static String getStatusBadgeColor(String runResult) {
        if (runResult.contains("✅ PASSED")) return "#28a745";
        if (runResult.contains("❌ FAILED")) return "#dc3545";
        return "#ffc107";
    }

    private static String generateFailureReason(LreTestRunModel model, LreRunStatusExtended runStatus, ThresholdResult thresholds) {
        StringBuilder reason = new StringBuilder();
        reason.append("The test ");

        if (thresholds.errorExceeded() || thresholds.failedTxnExceeded() || model.isTestFailed()) {
            reason.append("was marked as <strong>FAILED</strong> because ");

            boolean first = true;

            if (thresholds.errorExceeded()) {
                reason.append("the number of errors observed (")
                        .append(runStatus.getErrors())
                        .append(") exceeded the defined threshold of ")
                        .append(model.getMaxErrors());
                first = false;
            }

            if (thresholds.failedTxnExceeded()) {
                if (!first) reason.append(" and ");
                reason.append("the number of failed transactions observed (")
                        .append(runStatus.getTransFailed())
                        .append(") exceeded the defined threshold of ")
                        .append(model.getMaxFailedTxns());
            }

            if (!thresholds.errorExceeded() && !thresholds.failedTxnExceeded() && model.isTestFailed()) {
                reason.append("the test was manually marked as failed in LoadRunner Enterprise.");
            } else {
                reason.append(".");
            }
        }

        return reason.toString();
    }

}
