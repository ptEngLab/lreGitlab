package com.lre.actions.apis;

import com.lre.actions.common.entities.base.auth.AuthenticationClient;
import com.lre.actions.common.entities.base.run.LreRunResponse;
import com.lre.actions.common.entities.base.run.LreRunStatus;
import com.lre.actions.common.entities.base.test.Test;
import com.lre.actions.common.entities.base.testSet.LreTestSet;
import com.lre.actions.common.entities.base.testSet.LreTestSetFolder;
import com.lre.actions.common.entities.base.testinstance.LreTestInstance;
import com.lre.actions.common.entities.base.timeslot.TimeslotCheckResponse;
import com.lre.actions.httpclient.HttpClientUtils;
import com.lre.actions.httpclient.HttpRequestExecutor;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
public class LreRestApis implements AutoCloseable {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";

    private final CloseableHttpClient httpClient;
    private final String baseUrl;
    private final String lreApiUrl;
    private final String lreWebUrl;
    private final String domain;
    private final String project;

    public LreRestApis(LreTestRunModel model) {
        this.baseUrl = String.format(LRE_API_BASE_URL, model.getLreServerUrl());
        this.domain = model.getDomain();
        this.project = model.getProject();
        this.lreApiUrl = String.format(LRE_API_RESOURCES, this.baseUrl, domain, project);
        this.lreWebUrl = String.format(LRE_API_WEB_URL, model.getLreServerUrl());
        this.httpClient = HttpClientUtils.createClient();
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    private ClassicRequestBuilder createJsonRequest(String url, String method) {
        ClassicRequestBuilder builder;
        switch (method.toUpperCase()) {
            case GET -> builder = ClassicRequestBuilder.get(url);
            case POST -> builder = ClassicRequestBuilder.post(url);
            case PUT -> builder = ClassicRequestBuilder.put(url);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }
        return builder.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    }

    private ClassicRequestBuilder createXmlRequest(String url, String method) {
        ClassicRequestBuilder builder;
        switch (method.toUpperCase()) {
            case POST -> builder = ClassicRequestBuilder.post(url);
            case PUT -> builder = ClassicRequestBuilder.put(url);
            default -> throw new IllegalArgumentException("Unsupported XML method: " + method);
        }
        return builder.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_XML.getMimeType());
    }

    protected String sendRequest(ClassicRequestBuilder requestBuilder) {
        return HttpRequestExecutor.sendRequest(httpClient, requestBuilder);
    }

    protected boolean downloadFile(ClassicRequestBuilder requestBuilder, String destPath) {
        return HttpRequestExecutor.downloadFile(httpClient, requestBuilder, destPath);
    }

    private URI buildUriWithQueries(String baseUrl, Map<String, String> params) {
        try {
            URIBuilder builder = new URIBuilder(baseUrl);
            params.forEach(builder::addParameter);
            return builder.build();
        } catch (URISyntaxException e) {
            log.error("Invalid URL: {}", baseUrl, e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResourceList(String resourceName, Class<T> clazz) {
        String url = String.format("%s/%s", lreApiUrl, resourceName);
        String response = sendRequest(createJsonRequest(url, GET));
        log.debug("Resource fetch [{}] - {} item(s) retrieved", resourceName, clazz.getSimpleName());
        return JsonUtils.fromJsonArray(response, clazz);
    }

    private <T> T getResourceById(String resourceName, int id, Class<T> clazz) {
        try {
            URI uri = new URIBuilder(lreApiUrl + "/" + resourceName + "/" + id).build();
            String response = sendRequest(createJsonRequest(uri.toString(), GET));
            log.debug("Resource fetch [{}] - {} item retrieved with ID {}", resourceName, clazz.getSimpleName(), id);
            return JsonUtils.fromJson(response, clazz);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for getResourceById: {}/{}", resourceName, id, e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResourceByQuery(String resourceName, String query, String queryValue, Class<T> clazz) {
        URI uri = buildUriWithQueries(lreApiUrl + "/" + resourceName, Map.of(query, queryValue));
        String response = sendRequest(createJsonRequest(uri.toString(), GET));
        log.debug("Resource fetch [{}] - {} items retrieved with query {}", resourceName, clazz.getSimpleName(), query);
        return JsonUtils.fromJsonArray(response, clazz);
    }

    public boolean login(String username, String password, boolean authenticateWithToken) {
        try {
            if (authenticateWithToken) return loginWithToken(username, password);
            return loginWithBasicAuth(username, password);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    private boolean loginWithToken(String username, String password) throws URISyntaxException {
        String authUrl = String.format("%s/%s", baseUrl, LRE_AUTHENTICATE_WITH_TOKEN);
        ClassicRequestBuilder requestBuilder = createJsonRequest(authUrl, POST);
        AuthenticationClient authClient = new AuthenticationClient(username, password);
        requestBuilder.setEntity(new StringEntity(JsonUtils.toJson(authClient), ContentType.APPLICATION_JSON));
        sendRequest(requestBuilder);
        log.debug("Authentication successful with token");
        loginToWebProject();
        return true;
    }

    private boolean loginWithBasicAuth(String username, String password) throws URISyntaxException {
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        String authUrl = String.format("%s/%s", baseUrl, LRE_AUTHENTICATE_WITH_USERNAME);
        ClassicRequestBuilder requestBuilder = createJsonRequest(authUrl, GET);
        requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        sendRequest(requestBuilder);
        log.debug("Authentication successful with username/password");
        loginToWebProject();
        return true;
    }

    public void loginToWebProject() {
        Map<String, String> params = Map.of("domain", domain, "project", project);
        URI uri = buildUriWithQueries(lreWebUrl + "/" + LRE_WEB_LOGIN_TO_PROJECT, params);
        ClassicRequestBuilder requestBuilder = createJsonRequest(uri.toString(), GET);
        sendRequest(requestBuilder);
        log.debug("Web login to project successful for project: {}/{}", domain, project);
    }

    public void logout() {
        String logoutUrl = String.format("%s/%s", baseUrl, LRE_LOGOUT);
        ClassicRequestBuilder requestBuilder = createJsonRequest(logoutUrl, GET);
        sendRequest(requestBuilder);
        log.debug("Logout successful");
    }

    public Test getTest(int testId) {
        return getResourceById(LRE_API_TESTS, testId, Test.class);
    }

    public List<Test> getAllTests() {
        return getResourceList(LRE_API_TESTS, Test.class);
    }

    public TimeslotCheckResponse calculateTimeslotAvailability(int testId, String payload) {
        URI uri = buildUriWithQueries(lreWebUrl + "/" + TIMESLOT_CHECK, Map.of("testId", String.valueOf(testId)));
        ClassicRequestBuilder requestBuilder = createJsonRequest(uri.toString(), POST);
        requestBuilder.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        String response = sendRequest(requestBuilder);
        return TimeslotCheckResponse.jsonToObject(response);
    }

    public List<com.lre.actions.common.entities.base.testinstance.LreTestInstance> getTestInstancesForTestId(int testId) {
        String query = String.format(TEST_INSTANCE_QUERY, testId);
        return getResourceByQuery(TEST_INSTANCES_NAME, QUERY_PARAM, query, com.lre.actions.common.entities.base.testinstance.LreTestInstance.class);
    }

    public List<LreTestSet> getAllTestSets() {
        return getResourceList(TEST_SETS_NAME, LreTestSet.class);
    }

    public List<LreTestSetFolder> getAllTestSetFolders() {
        return getResourceList(TEST_SET_FOLDERS_NAME, LreTestSetFolder.class);
    }

    public LreTestInstance createTestInstance(String payload) {
        try {
            URI uri = new URIBuilder(lreApiUrl + "/" + TEST_INSTANCES_NAME).build();
            ClassicRequestBuilder requestBuilder = createJsonRequest(uri.toString(), POST);
            requestBuilder.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
            String response = sendRequest(requestBuilder);
            log.debug("Create TestInstance response received: {}", response);
            return JsonUtils.fromJson(response, LreTestInstance.class);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for createTestInstance: {}", TEST_INSTANCES_NAME, e);
            throw new RuntimeException(e);
        }
    }

    public LreTestSet createTestSet(String payload) {
        try {
            URI uri = new URIBuilder(lreApiUrl + "/" + TEST_SETS_NAME).build();
            ClassicRequestBuilder requestBuilder = createJsonRequest(uri.toString(), POST);
            requestBuilder.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
            String response = sendRequest(requestBuilder);
            log.debug("Create TestSet response received: {}", response);
            return JsonUtils.fromJson(response, LreTestSet.class);

        } catch (URISyntaxException e) {
            log.error("Invalid URL for createTestInstance: {}", TEST_INSTANCES_NAME, e);
            throw new RuntimeException(e);
        }
    }

    public LreTestSetFolder createTestSetFolder(String payload) {
        try {
            URI uri = new URIBuilder(lreApiUrl + "/" + TEST_SET_FOLDERS_NAME).build();
            ClassicRequestBuilder requestBuilder = createJsonRequest(uri.toString(), POST);
            requestBuilder.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
            String response = sendRequest(requestBuilder);
            log.debug("Create TestSetFolder response received: {}", response);
            return JsonUtils.fromJson(response, LreTestSetFolder.class);

        } catch (URISyntaxException e) {
            log.error("Invalid URL for createTestInstance: {}", TEST_INSTANCES_NAME, e);
            throw new RuntimeException(e);
        }
    }

    public LreRunResponse startRun(int testId, String payload) {
        URI uri = buildUriWithQueries(lreWebUrl + "/" + START_RUN_API, Map.of("testId", String.valueOf(testId)));
        ClassicRequestBuilder requestBuilder = createJsonRequest(uri.toString(), POST);
        requestBuilder.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        String response = sendRequest(requestBuilder);
        log.debug("Start run response received: {}", response);
        return JsonUtils.fromJson(response, LreRunResponse.class);
    }

    public LreRunStatus getRunStatus(int runId){
        return getResourceById(RUN_STATUS_API, runId, LreRunStatus.class);
    }

}
