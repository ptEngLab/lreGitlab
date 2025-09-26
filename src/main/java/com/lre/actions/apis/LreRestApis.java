package com.lre.actions.apis;

import com.lre.model.run.LreRunResponse;
import com.lre.model.run.LreRunStatus;
import com.lre.model.test.testcontent.groups.script.Script;
import com.lre.model.testSet.LreTestSet;
import com.lre.model.testSet.LreTestSetFolder;
import com.lre.model.testinstance.LreTestInstance;
import com.lre.model.testplan.LreTestPlan;
import com.lre.model.timeslot.TimeslotCheckResponse;
import com.lre.actions.httpclient.HttpClientUtils;
import com.lre.model.test.Test;
import com.lre.actions.runmodel.LreTestRunModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.List;
import java.util.Map;

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
public class LreRestApis implements AutoCloseable {

    private final CloseableHttpClient httpClient;
    private final ApiUrlBuilder urlBuilder;
    private final ApiRequestExecutor requestExecutor;
    private final AuthenticationService authService;

    public LreRestApis(LreTestRunModel model) {
        this.httpClient = HttpClientUtils.createClient();
        this.urlBuilder = new ApiUrlBuilder(model);
        this.requestExecutor = new ApiRequestExecutor(httpClient);
        this.authService = new AuthenticationService(httpClient, urlBuilder);
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    // Authentication methods
    public boolean login(String username, String password, boolean authenticateWithToken) {
        return authService.login(username, password, authenticateWithToken);
    }

    public void logout() {
        authService.logout();
    }

    // Test operations
    public Test getTest(int testId) {
        return requestExecutor.getResourceById(urlBuilder.getTestByIdUrl(testId),
                Test.class, "test by id");
    }

    public List<Test> getAllTests() {
        return requestExecutor.getResourceList(urlBuilder.getTestsUrl(),
                Test.class, "tests");
    }

    public Test createNewTest(String payload) {
        return requestExecutor.createResource(urlBuilder.getTestsUrl(), payload,
                Test.class, "create new test");
    }

    public void updateTest(int testId, String payload) {
        requestExecutor.updateResource(urlBuilder.getTestByIdUrl(testId), payload, ContentType.APPLICATION_XML);
    }

    // Test Plan operations
    public List<LreTestPlan> getAllTestPlans() {
        return requestExecutor.getResourceList(urlBuilder.getTestPlansUrl(),
                LreTestPlan.class, "test plans");
    }

    public LreTestPlan createNewTestPlan(String payload) {
        return requestExecutor.createResource(urlBuilder.getTestPlansUrl(), payload,
                LreTestPlan.class, "test plan");
    }

    // Test Set operations
    public List<LreTestSet> getAllTestSets() {
        return requestExecutor.getResourceList(urlBuilder.getTestSetsUrl(),
                LreTestSet.class, "test sets");
    }

    public LreTestSet createTestSet(String payload) {
        return requestExecutor.createResource(urlBuilder.getTestSetsUrl(), payload,
                LreTestSet.class, "test set");
    }

    // Test Set Folder operations
    public List<LreTestSetFolder> getAllTestSetFolders() {
        return requestExecutor.getResourceList(urlBuilder.getTestSetFoldersUrl(),
                LreTestSetFolder.class, "test set folders");
    }

    public LreTestSetFolder createTestSetFolder(String payload) {
        return requestExecutor.createResource(urlBuilder.getTestSetFoldersUrl(), payload,
                LreTestSetFolder.class, "test set folder");
    }

    // Test Instance operations
    public List<LreTestInstance> getTestInstancesForTestId(int testId) {
        return requestExecutor.getResourceByQuery(
                urlBuilder.getTestInstancesUrl(),
                Map.of(QUERY_PARAM, String.format(TEST_INSTANCE_QUERY, testId)),
                LreTestInstance.class,
                "test instances"
        );
    }

    public List<Script> getAllScripts() {
        return requestExecutor.getResourceList(urlBuilder.getScriptsUrl(),
                Script.class, "scripts");
    }

    public Script getScriptById(int testId) {
        return requestExecutor.getResourceById(urlBuilder.getScriptByIdUrl(testId),
                Script.class, "scripts");
    }

    public LreTestInstance createTestInstance(String payload) {
        return requestExecutor.createResource(urlBuilder.getTestInstancesUrl(), payload,
                LreTestInstance.class, "test instance");
    }

    // Run operations
    public LreRunStatus getRunStatus(int runId) {
        return requestExecutor.getResourceById(urlBuilder.getRunStatusUrl(runId),
                LreRunStatus.class, "run status");
    }

    public LreRunResponse startRun(int testId, String payload) {
        return requestExecutor.postWithQueryParams(
                urlBuilder.getStartRunUrl(),
                Map.of("testId", String.valueOf(testId)),
                payload,
                LreRunResponse.class,
                "start run"
        );
    }

    // Timeslot operations
    public TimeslotCheckResponse calculateTimeslotAvailability(int testId, String payload) {
        return requestExecutor.postWithQueryParams(
                urlBuilder.getTimeslotCheckUrl(),
                Map.of("testId", String.valueOf(testId)),
                payload,
                TimeslotCheckResponse.class,
                "timeslot check"
        );
    }

}