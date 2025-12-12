package com.lre.client.api.builder;

import static com.lre.common.constants.ConfigConstants.*;

/**
 * Builder for run-related URLs
 */
public record RunUrlBuilder(String lreApiUrl, String lreWebUrl, String lreServerUrl) {

    public String getRunStatusUrl(int runId) {
        return UrlUtils.pathWithSubResource(lreApiUrl, RUN_STATUS_API, runId, "Extended");
    }

    public String getStartRunUrl() {
        return UrlUtils.path(lreWebUrl, START_RUN_API);
    }

    public String getStartRunUrl(int testId) {
        return UrlUtils.withQuery(getStartRunUrl(), "testId", String.valueOf(testId));
    }

    public String getAbortRunUrl(int runId) {
        return UrlUtils.pathWithSubResource(lreApiUrl, RUN_STATUS_API, runId, ABORT_RUN_API);
    }

    public String getTimeslotCheckUrl() {
        return UrlUtils.path(lreWebUrl, TIMESLOT_CHECK);
    }

    public String getTimeslotCheckUrl(int testId) {
        return UrlUtils.withQuery(getTimeslotCheckUrl(), "testId", String.valueOf(testId));
    }

    public String getRunResultsUrl(int runId) {
        return UrlUtils.pathWithSubResource(lreApiUrl, RUN_STATUS_API, runId, RESULTS_RESOURCE_API);
    }

    public String getRunResultsFileUrl(int runId, int resultId) {
        return UrlUtils.pathWithSubResourceAndData(lreApiUrl, RUN_STATUS_API, runId, RESULTS_RESOURCE_API, resultId);
    }

    public String getRunResultsExtendedUrl() {
        return UrlUtils.path(lreWebUrl, RUN_STATUS_API + "/get");
    }

    public String getOpenRunDashboardUrl() {
        return UrlUtils.path(lreServerUrl, OPEN_DASHBOARD_URL);
    }

    public String getTransactionsDataUrl() {
        return UrlUtils.path(lreServerUrl, TRANSACTIONS_DATA_URL);
    }
}