package com.lre.services.lre.summary;

import com.lre.client.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunStatusExtended;

public record ThresholdResult(boolean errorExceeded, boolean failedTxnExceeded, String errorStr, String failedTxnStr,
                              String runResult) {

    private static final String WARNING_SYMBOL = " ⚠ ";
    private static final String EXCEEDED_TEXT = " — Exceeded";
    private static final String PASSED_SYMBOL = "✅ PASSED";
    private static final String FAILED_SYMBOL = "❌ FAILED";

    public static ThresholdResult checkThresholds(LreTestRunModel model, LreRunStatusExtended runStatusExtended) {
        long errorThreshold = model.getMaxErrors();
        long failedTxnThreshold = model.getMaxFailedTxns();

        boolean errorExceeded = runStatusExtended.getErrors() >= errorThreshold;
        boolean failedTxnExceeded = runStatusExtended.getTransFailed() >= failedTxnThreshold;

        String errorStr = errorExceeded
                ? runStatusExtended.getErrors() + WARNING_SYMBOL + "(limit: " + errorThreshold + ")" + EXCEEDED_TEXT
                : runStatusExtended.getErrors() + " (limit: " + errorThreshold + ")";

        String failedTxnStr = failedTxnExceeded
                ? runStatusExtended.getTransFailed() + WARNING_SYMBOL + "(limit: " + failedTxnThreshold + ")" + EXCEEDED_TEXT
                : runStatusExtended.getTransFailed() + " (limit: " + failedTxnThreshold + ")";

        String runResult = (errorExceeded || failedTxnExceeded || model.isTestFailed())
                ? FAILED_SYMBOL
                : PASSED_SYMBOL;

        return new ThresholdResult(errorExceeded, failedTxnExceeded, errorStr, failedTxnStr, runResult);
    }
}