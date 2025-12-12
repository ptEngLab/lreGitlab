package com.lre.client.api.builder;

import static com.lre.common.constants.ConfigConstants.*;

/**
 * Builder for test-related URLs
 */
public record TestUrlBuilder(String lreApiUrl) {

    public String getTestsUrl() {
        return UrlUtils.path(lreApiUrl, LRE_API_TESTS);
    }

    public String getTestByIdUrl(int testId) {
        return UrlUtils.pathWithId(lreApiUrl, LRE_API_TESTS, testId);
    }

    public String getTestPlansUrl() {
        return UrlUtils.path(lreApiUrl, TEST_PLAN_NAME);
    }

    public String getTestSetsUrl() {
        return UrlUtils.path(lreApiUrl, TEST_SETS_NAME);
    }

    public String getTestSetFoldersUrl() {
        return UrlUtils.path(lreApiUrl, TEST_SET_FOLDERS_NAME);
    }

    public String getTestInstancesUrl() {
        return UrlUtils.path(lreApiUrl, TEST_INSTANCES_NAME);
    }

    public String getTestInstancesByTestIdUrl(int testId) {
        return UrlUtils.withQuery(
                getTestInstancesUrl(),
                QUERY_PARAM,
                String.format(TEST_INSTANCE_QUERY, testId)
        );
    }
}