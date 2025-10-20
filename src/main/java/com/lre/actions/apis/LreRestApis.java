package com.lre.actions.apis;

import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.core.http.HttpClientUtils;
import com.lre.model.run.LreRunResponse;
import com.lre.model.run.LreRunResult;
import com.lre.model.run.LreRunStatus;
import com.lre.model.run.LreRunStatusExtended;
import com.lre.model.script.LreScript;
import com.lre.model.test.Test;
import com.lre.model.test.testcontent.groups.hosts.CloudTemplate;
import com.lre.model.test.testcontent.groups.hosts.HostResponse;
import com.lre.model.test.testcontent.groups.script.Script;
import com.lre.model.testSet.LreTestSet;
import com.lre.model.testSet.LreTestSetFolder;
import com.lre.model.testinstance.LreTestInstance;
import com.lre.model.testplan.LreTestPlan;
import com.lre.model.timeslot.TimeslotCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;

import java.nio.file.Path;
import java.util.List;

@Slf4j
public class LreRestApis implements AutoCloseable {

    private final CloseableHttpClient httpClient;
    private final ApiUrlBuilderLre urlBuilder;
    private final ApiRequestExecutor executor;
    private final AuthenticationService authService;

    public LreRestApis(LreTestRunModel model) {
        this.httpClient = HttpClientUtils.createClient();
        this.urlBuilder = new ApiUrlBuilderLre(model);
        this.executor = new ApiRequestExecutor(httpClient);
        this.authService = new AuthenticationService(httpClient, urlBuilder);
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) httpClient.close();
    }

    // Authentication
    public boolean login(String username, String password, boolean useToken) {
        return authService.login(username, password, useToken);
    }

    public void logout() {
        authService.logout();
    }

    // Test
    public Test fetchTest(int testId) {
        return executor.fetchById(urlBuilder.getTestByIdUrl(testId), Test.class, "Test");
    }

    public List<Test> fetchAllTests() {
        return executor.fetchList(urlBuilder.getTestsUrl(), Test.class, "Tests");
    }

    public Test createTest(String payload) {
        return executor.create(urlBuilder.getTestsUrl(), payload, ContentType.APPLICATION_XML, Test.class, "Test");
    }

    public void updateTest(int testId, String payload) {
        executor.update(urlBuilder.getTestByIdUrl(testId), payload, ContentType.APPLICATION_XML);
    }

    // Test Plan
    public List<LreTestPlan> fetchAllTestPlans() {
        return executor.fetchList(urlBuilder.getTestPlansUrl(), LreTestPlan.class, "Test Plans");
    }

    public LreTestPlan createTestPlan(String payload) {
        return executor.create(urlBuilder.getTestPlansUrl(), payload, ContentType.APPLICATION_JSON, LreTestPlan.class, "Test Plan");
    }

    // Test Set
    public List<LreTestSet> fetchAllTestSets() {
        return executor.fetchList(urlBuilder.getTestSetsUrl(), LreTestSet.class, "Test Sets");
    }

    public LreTestSet createTestSet(String payload) {
        return executor.create(urlBuilder.getTestSetsUrl(), payload, ContentType.APPLICATION_JSON, LreTestSet.class, "Test Set");
    }

    // Test Set Folder
    public List<LreTestSetFolder> fetchAllTestSetFolders() {
        return executor.fetchList(urlBuilder.getTestSetFoldersUrl(), LreTestSetFolder.class, "Test Set Folders");
    }

    public LreTestSetFolder createTestSetFolder(String payload) {
        return executor.create(urlBuilder.getTestSetFoldersUrl(), payload, ContentType.APPLICATION_JSON, LreTestSetFolder.class, "Test Set Folder");
    }

    // Test Instance
    public List<LreTestInstance> fetchTestInstances(int testId) {
        String url = urlBuilder.getTestInstancesByTestIdUrl(testId);
        return executor.fetchList(url, LreTestInstance.class, "Test Instances");
    }

    public LreTestInstance createTestInstance(String payload) {
        return executor.create(urlBuilder.getTestInstancesUrl(), payload, ContentType.APPLICATION_JSON, LreTestInstance.class, "Test Instance");
    }

    // Script
    public List<Script> fetchAllScripts() {
        return executor.fetchList(urlBuilder.getScriptsUrl(), Script.class, "Scripts");
    }

    public Script fetchScriptById(int scriptId) {
        return executor.fetchById(urlBuilder.getScriptByIdUrl(scriptId), Script.class, "Script");
    }

    // Run
    public LreRunStatus fetchRunStatus(int runId) {
        return executor.fetchById(urlBuilder.getRunStatusUrl(runId), LreRunStatus.class, "Run Status");
    }

    public LreRunResponse startRun(int testId, String payload) {
        String url = urlBuilder.getStartRunUrl(testId);
        return executor.create(url, payload, ContentType.APPLICATION_JSON, LreRunResponse.class, "Start Run");
    }

    public void abortRun(int runId) {
        executor.create(urlBuilder.getAbortRunUrl(runId), "{}", ContentType.APPLICATION_JSON, Void.class, "Abort Run");
    }

    public List<LreRunStatusExtended> fetchRunResultsExtended(String payload) {
        return executor.createList(urlBuilder.getRunResultsExtendedUrl(), payload, ContentType.APPLICATION_JSON, LreRunStatusExtended.class, "Run status extended");
    }

    public List<LreRunResult> fetchRunResults(int runId) {
        return executor.fetchList(urlBuilder.getRunResultsUrl(runId), LreRunResult.class, "Run Results");
    }

    // Timeslot
    public TimeslotCheckResponse calculateTimeslotAvailability(int testId, String payload) {
        String url = urlBuilder.getTimeslotCheckUrl(testId);
        return executor.create(url, payload, ContentType.APPLICATION_JSON, TimeslotCheckResponse.class, "Timeslot Check");
    }

    // Cloud Templates
    public List<CloudTemplate> fetchAllCloudTemplates() {
        return executor.fetchList(urlBuilder.getCloudTemplateUrl(), CloudTemplate.class, "Cloud Templates");
    }

    public CloudTemplate fetchCloudTemplateById(int id) {
        return executor.fetchById(urlBuilder.getCloudTemplateByIdUrl(id), CloudTemplate.class, "Cloud Template");
    }

    // Hosts
    public List<HostResponse> fetchControllers() {
        String url = urlBuilder.getControllersUrl();
        return executor.fetchList(url, HostResponse.class, "Controllers list");
    }

    public List<HostResponse> fetchLoadGenerators() {
        String url = urlBuilder.getLoadGeneratorsUrl();
        return executor.fetchList(url, HostResponse.class, "Load Generators list");
    }

    public boolean getRunResultData(int runId, int resultsId, String filePath) {
        String url = urlBuilder.getRunResultsFileUrl(runId, resultsId);
        return executor.download(url, filePath);
    }

    public LreScript uploadScript(Path scriptPath, String payload) {
        String url = urlBuilder.getUploadScriptUrl();
        return executor.upload(url, payload, scriptPath.toFile(), LreScript.class, "Upload");

    }
}