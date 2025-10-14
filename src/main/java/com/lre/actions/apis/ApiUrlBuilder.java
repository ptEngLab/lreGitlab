package com.lre.actions.apis;

import com.lre.actions.runmodel.LreTestRunModel;
import lombok.Getter;

import static com.lre.actions.utils.ConfigConstants.*;

public class ApiUrlBuilder {
    private final String lreApiUrl;
    private final String lreWebUrl;
    private final String baseUrl;
    @Getter
    private final String domain;
    @Getter
    private final String project;

    public ApiUrlBuilder(LreTestRunModel model) {
        this.baseUrl = String.format(LRE_API_BASE_URL, model.getLreServerUrl());
        this.domain = model.getDomain();
        this.project = model.getProject();
        this.lreApiUrl = String.format(LRE_API_RESOURCES, this.baseUrl, domain, project);
        this.lreWebUrl = String.format(LRE_API_WEB_URL, model.getLreServerUrl());
    }

    public String getTestByIdUrl(int testId) {
        return lreApiUrl + "/" + LRE_API_TESTS + "/" + testId;
    }

    public String getTestsUrl() {
        return lreApiUrl + "/" + LRE_API_TESTS;
    }

    public String getScriptsUrl() {
        return lreApiUrl + "/" + SCRIPTS_RESOURCE_NAME;
    }

    public String getScriptByIdUrl(int testId) {
        return lreApiUrl + "/" + SCRIPTS_RESOURCE_NAME + "/" + testId;
    }

    public String getTestPlansUrl() {
        return lreApiUrl + "/" + TEST_PLAN_NAME;
    }

    public String getTestSetsUrl() {
        return lreApiUrl + "/" + TEST_SETS_NAME;
    }

    public String getTestSetFoldersUrl() {
        return lreApiUrl + "/" + TEST_SET_FOLDERS_NAME;
    }

    public String getTestInstancesUrl() {
        return lreApiUrl + "/" + TEST_INSTANCES_NAME;
    }

    public String getRunStatusUrl(int runId) {
        return lreApiUrl + "/" + RUN_STATUS_API + "/" + runId + "/Extended";
    }

    public String getStartRunUrl() {
        return lreWebUrl + "/" + START_RUN_API;
    }

    public String getAbortRunUrl(int runId) {
        return lreApiUrl + "/" + RUN_STATUS_API + "/" + runId + "/"  + ABORT_RUN_API;
    }

    public String getTimeslotCheckUrl() {
        return lreWebUrl + "/" + TIMESLOT_CHECK;
    }

    public String getAuthUrl(String endpoint) {
        return baseUrl + "/" + endpoint;
    }

    public String getWebLoginUrl() {
        return lreWebUrl + "/" + LRE_WEB_LOGIN_TO_PROJECT;
    }

    public String getCloudTemplateUrl() {
        return lreApiUrl + "/" + CLOUD_TEMPLATE_RESOURCE_NAME;
    }

    public String getCloudTemplateByIdUrl(int id) {
        return lreApiUrl + "/" + CLOUD_TEMPLATE_RESOURCE_NAME + "/" + id;
    }

    public String getHostsUrl() {
        return String.format("%s/%s", lreApiUrl, HOST_RESOURCE_API);
    }

    public String getRunResultsUrl(int runId) {
        return String.format("%s/%s/%d/%s", lreApiUrl, RUN_STATUS_API, runId, RESULTS_RESOURCE_API);
    }

    public String getRunResultsFileUrl(int runId, int resultId){
        return String.format("%s/%s/%d/%s/%d/data", lreApiUrl, RUN_STATUS_API, runId, RESULTS_RESOURCE_API, resultId);
    }

    public String getRunResultsExtendedUrl(){
        return String.format("%s/%s/get", lreWebUrl, RUN_STATUS_API);
    }

}