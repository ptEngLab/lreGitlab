package com.lre.services.lre.report.renderer.text;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.services.lre.summary.ThresholdResult;

import static com.lre.common.utils.CommonUtils.calculateTestDuration;
import static com.lre.common.utils.CommonUtils.formatDateTime;

public class TextSummaryGenerator {
    public static String[][] generate(LreTestRunModel model, LreRunStatusExtended runStatusExtended, ThresholdResult thresholds) {
        return new String[][]{
                {
                        "Domain: " + model.getDomain(),
                        "Project: " + model.getProject(),
                        "Test Name: " + model.getTestName(),
                        "Test Id: " + model.getTestId()
                },
                {
                        "Test Folder: " + model.getTestFolderPath(),
                        "Test Instance Id: " + runStatusExtended.getTestInstanceId(),
                        "Run Name: " + runStatusExtended.getName(),
                        "Run Status: " + runStatusExtended.getState() + ", Result: " + thresholds.runResult()
                },
                {
                        "Start Time: " + formatDateTime(runStatusExtended.getStart()),
                        "End Time: " + formatDateTime(runStatusExtended.getEnd()),
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
                }
        };
    }
}
