package com.lre.actions.apis;

import com.lre.actions.httpclient.HttpRequestExecutor;
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
import java.util.List;
import java.util.Map;

@Slf4j
public class ApiRequestExecutor {
    private final CloseableHttpClient httpClient;

    public ApiRequestExecutor(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public <T> T getResourceById(String url, Class<T> clazz, String resourceName) {
        try {
            String response = sendRequest(buildGetRequest(url));
            log.debug("Fetched {} by ID: {}", resourceName, clazz.getSimpleName());
            return JsonUtils.fromJson(response, clazz);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for {}: {}", resourceName, url, e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> getResourceList(String url, Class<T> clazz, String resourceName) {
        try {
            String response = sendRequest(buildGetRequest(url));
            log.debug("Fetched {} list: {} items", resourceName, clazz.getSimpleName());
            return JsonUtils.fromJsonArray(response, clazz);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> getResourceByQuery(String baseUrl, Map<String, String> params,
                                          Class<T> clazz, String resourceName) {
        try {
            URI uri = buildUriWithQueries(baseUrl, params);
            String response = sendRequest(buildGetRequest(uri.toString()));
            log.debug("Fetched {} by query: {} items", resourceName, clazz.getSimpleName());
            return JsonUtils.fromJsonArray(response, clazz);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for {} query: {}", resourceName, baseUrl, e);
            throw new RuntimeException(e);
        }
    }

    public <T> T createResource(String url, String payload, Class<T> clazz, String resourceName) {
        try {
            ClassicRequestBuilder request = buildPostRequest(url, payload);
            String response = sendRequest(request);
            log.debug("Created {}: {}", resourceName, response);
            return JsonUtils.fromJson(response, clazz);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for {} creation: {}", resourceName, url, e);
            throw new RuntimeException(e);
        }
    }

    public <T> T postWithQueryParams(String baseUrl, Map<String, String> params, String payload,
                                     Class<T> clazz, String operation) {
        try {
            URI uri = buildUriWithQueries(baseUrl, params);
            ClassicRequestBuilder request = buildPostRequest(uri.toString(), payload);
            String response = sendRequest(request);
            log.debug("{} response: {}", operation, response);
            return JsonUtils.fromJson(response, clazz);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for {}: {}", operation, baseUrl, e);
            throw new RuntimeException(e);
        }
    }

    public void updateResource(String url, String payload, ContentType contentType) {
        try {
            ClassicRequestBuilder request = buildPutRequest(url, payload, contentType);
            sendRequest(request);
            log.debug("Updated resource: {}", url);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for update: {}", url, e);
            throw new RuntimeException(e);
        }
    }

    private ClassicRequestBuilder buildGetRequest(String url) throws URISyntaxException {
        return ClassicRequestBuilder.get(new URI(url))
                .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    }

    private ClassicRequestBuilder buildPostRequest(String url, String payload)
            throws URISyntaxException {
        return ClassicRequestBuilder.post(new URI(url))
                .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
    }

    private ClassicRequestBuilder buildPutRequest(String url, String payload, ContentType contentType)
            throws URISyntaxException {
        return ClassicRequestBuilder.put(new URI(url))
                .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
                .addHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType())
                .setEntity(new StringEntity(payload, contentType));
    }

    private URI buildUriWithQueries(String baseUrl, Map<String, String> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(baseUrl);
        params.forEach(builder::addParameter);
        return builder.build();
    }

    private String sendRequest(ClassicRequestBuilder requestBuilder) {
        return HttpRequestExecutor.sendRequest(httpClient, requestBuilder);
    }
}