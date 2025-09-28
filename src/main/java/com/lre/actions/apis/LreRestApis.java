package com.lre.actions.apis;

import com.lre.actions.httpclient.HttpClientUtils;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.model.run.LreRunResponse;
import com.lre.model.run.LreRunStatus;
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
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
public class LreRestApis implements AutoCloseable {

    private final CloseableHttpClient httpClient;
    private final ApiUrlBuilder urlBuilder;
    private final ApiRequestExecutor executor;
    private final AuthenticationService authService;

    public LreRestApis(LreTestRunModel model) {
        this.httpClient = HttpClientUtils.createClient();
        this.urlBuilder = new ApiUrlBuilder(model);
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
        return executor.create(urlBuilder.getTestsUrl(), payload, Test.class, "Test");
    }

    public void updateTest(int testId, String payload) {
        executor.update(urlBuilder.getTestByIdUrl(testId), payload, ContentType.APPLICATION_XML);
    }

    // Test Plan
    public List<LreTestPlan> fetchAllTestPlans() {
        return executor.fetchList(urlBuilder.getTestPlansUrl(), LreTestPlan.class, "Test Plans");
    }

    public LreTestPlan createTestPlan(String payload) {
        return executor.create(urlBuilder.getTestPlansUrl(), payload, LreTestPlan.class, "Test Plan");
    }

    // Test Set
    public List<LreTestSet> fetchAllTestSets() {
        return executor.fetchList(urlBuilder.getTestSetsUrl(), LreTestSet.class, "Test Sets");
    }

    public LreTestSet createTestSet(String payload) {
        return executor.create(urlBuilder.getTestSetsUrl(), payload, LreTestSet.class, "Test Set");
    }

    // Test Set Folder
    public List<LreTestSetFolder> fetchAllTestSetFolders() {
        return executor.fetchList(urlBuilder.getTestSetFoldersUrl(), LreTestSetFolder.class, "Test Set Folders");
    }

    public LreTestSetFolder createTestSetFolder(String payload) {
        return executor.create(urlBuilder.getTestSetFoldersUrl(), payload, LreTestSetFolder.class, "Test Set Folder");
    }

    // Test Instance
    public List<LreTestInstance> fetchTestInstances(int testId) {
        return executor.fetchByQuery(
                urlBuilder.getTestInstancesUrl(),
                Map.of(QUERY_PARAM, String.format(TEST_INSTANCE_QUERY, testId)),
                LreTestInstance.class,
                "Test Instances"
        );
    }

    public LreTestInstance createTestInstance(String payload) {
        return executor.create(urlBuilder.getTestInstancesUrl(), payload, LreTestInstance.class, "Test Instance");
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
        return executor.postWithQuery(
                urlBuilder.getStartRunUrl(),
                Map.of("testId", String.valueOf(testId)),
                payload,
                LreRunResponse.class,
                "Start Run"
        );
    }

    // Timeslot
    public TimeslotCheckResponse calculateTimeslotAvailability(int testId, String payload) {
        return executor.postWithQuery(
                urlBuilder.getTimeslotCheckUrl(),
                Map.of("testId", String.valueOf(testId)),
                payload,
                TimeslotCheckResponse.class,
                "Timeslot Check"
        );
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
        String url = urlBuilder.getHostsUrl();
        String queryValue = "{Purpose['*Controller*'];State['Operational']}";
        String fullUrl = url + "?query=" + URLEncoder.encode(queryValue, StandardCharsets.UTF_8);
        return executor.fetchList(fullUrl, HostResponse.class, "Controllers list");
    }


    public List<HostResponse> fetchLoadGenerators() {
        String url = urlBuilder.getHostsUrl();
        String queryValue = "{Purpose['*Load Generator*'];State['Operational']}";
        String fullUrl = url + "?query=" + URLEncoder.encode(queryValue, StandardCharsets.UTF_8);
        return executor.fetchList(fullUrl, HostResponse.class, "Load Generators list");
    }
}
