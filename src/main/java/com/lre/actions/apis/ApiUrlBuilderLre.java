package com.lre.actions.apis;

import com.lre.actions.runmodel.LreTestRunModel;
import lombok.Getter;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;

import static com.lre.actions.utils.ConfigConstants.*;

@Getter
public class ApiUrlBuilderLre {
    private final String lreApiUrl;
    private final String lreWebUrl;
    private final String baseUrl;
    private final String domain;
    private final String project;

    public ApiUrlBuilderLre(LreTestRunModel model) {
        this.baseUrl = String.format(LRE_API_BASE_URL, model.getLreServerUrl());
        this.domain = model.getDomain();
        this.project = model.getProject();
        this.lreApiUrl = String.format(LRE_API_RESOURCES, this.baseUrl, domain, project);
        this.lreWebUrl = String.format(LRE_API_WEB_URL, model.getLreServerUrl());
    }

    // Base URLs
    public String getTestByIdUrl(int testId) {
        return String.format("%s/%s/%d", lreApiUrl, LRE_API_TESTS, testId);
    }

    public String getTestsUrl() {
        return String.format("%s/%s", lreApiUrl, LRE_API_TESTS);
    }

    public String getScriptsUrl() {
        return String.format("%s/%s", lreApiUrl, SCRIPTS_RESOURCE_NAME);
    }

    public String getScriptByIdUrl(int scriptId) {
        return String.format("%s/%s/%d", lreApiUrl, SCRIPTS_RESOURCE_NAME, scriptId);
    }

    public String getTestPlansUrl() {
        return String.format("%s/%s", lreApiUrl, TEST_PLAN_NAME);
    }

    public String getTestSetsUrl() {
        return String.format("%s/%s", lreApiUrl, TEST_SETS_NAME);
    }

    public String getTestSetFoldersUrl() {
        return String.format("%s/%s", lreApiUrl, TEST_SET_FOLDERS_NAME);
    }

    public String getTestInstancesUrl() {
        return String.format("%s/%s", lreApiUrl, TEST_INSTANCES_NAME);
    }

    // Test Instances with query
    public String getTestInstancesByTestIdUrl(int testId) {
        try {
            String baseUrl = getTestInstancesUrl();
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter(QUERY_PARAM, String.format(TEST_INSTANCE_QUERY, testId));
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for test instances", e);
        }
    }

    public String getRunStatusUrl(int runId) {
        return String.format("%s/%s/%d/Extended", lreApiUrl, RUN_STATUS_API, runId);
    }

    public String getStartRunUrl() {
        return String.format("%s/%s", lreWebUrl, START_RUN_API);
    }

    // Start Run with testId parameter
    public String getStartRunUrl(int testId) {
        try {
            String baseUrl = getStartRunUrl();
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter("testId", String.valueOf(testId));
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for start run", e);
        }
    }

    public String getAbortRunUrl(int runId) {
        return String.format("%s/%s/%d/%s", lreApiUrl, RUN_STATUS_API, runId, ABORT_RUN_API);
    }

    public String getTimeslotCheckUrl() {
        return String.format("%s/%s", lreWebUrl, TIMESLOT_CHECK);
    }

    // Timeslot Check with testId parameter
    public String getTimeslotCheckUrl(int testId) {
        try {
            String baseUrl = getTimeslotCheckUrl();
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter("testId", String.valueOf(testId));
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for timeslot check", e);
        }
    }

    public String getAuthUrl(String endpoint) {
        return String.format("%s/%s", baseUrl, endpoint);
    }

    public String getWebLoginUrl() {
        return String.format("%s/%s", lreWebUrl, LRE_WEB_LOGIN_TO_PROJECT);
    }

    public String getCloudTemplateUrl() {
        return String.format("%s/%s", lreApiUrl, CLOUD_TEMPLATE_RESOURCE_NAME);
    }

    public String getCloudTemplateByIdUrl(int id) {
        return String.format("%s/%s/%d", lreApiUrl, CLOUD_TEMPLATE_RESOURCE_NAME, id);
    }

    public String getHostsUrl() {
        return String.format("%s/%s", lreApiUrl, HOST_RESOURCE_API);
    }

    // Hosts with query parameters
    public String getControllersUrl() {
        try {
            String baseUrl = getHostsUrl();
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter("query", "{Purpose['*Controller*'];State['Operational']}");
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for controllers", e);
        }
    }

    public String getLoadGeneratorsUrl() {
        try {
            String baseUrl = getHostsUrl();
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.addParameter("query", "{Purpose['*Load Generator*'];State['Operational']}");
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL for load generators", e);
        }
    }

    public String getRunResultsUrl(int runId) {
        return String.format("%s/%s/%d/%s", lreApiUrl, RUN_STATUS_API, runId, RESULTS_RESOURCE_API);
    }

    public String getRunResultsFileUrl(int runId, int resultId) {
        return String.format("%s/%s/%d/%s/%d/data", lreApiUrl, RUN_STATUS_API, runId, RESULTS_RESOURCE_API, resultId);
    }

    public String getRunResultsExtendedUrl() {
        return String.format("%s/%s/get", lreWebUrl, RUN_STATUS_API);
    }

    public String getUploadScriptUrl() {
            return String.format("%s/%s", lreApiUrl, SCRIPTS_RESOURCE_NAME);
    }
}