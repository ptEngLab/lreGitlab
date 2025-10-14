package com.lre.actions.apis;

import com.lre.core.http.HttpRequestExecutor;
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
public record ApiRequestExecutor(CloseableHttpClient httpClient) {

    private enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    // Public API Methods
    public <T> T fetchById(String url, Class<T> clazz, String resourceName) {
        return execute(HttpMethod.GET, url, null, null, clazz, resourceName + " by ID");
    }

    public <T> List<T> fetchList(String url, Class<T> clazz, String resourceName) {
        return executeList(url, clazz, resourceName + " list");
    }

    public <T> List<T> fetchByQuery(String baseUrl, Map<String, String> params, Class<T> clazz, String resourceName) {
        String url = buildUrlWithParams(baseUrl, params);
        return executeList(url, clazz, resourceName + " by query");
    }

    public <T> T create(String url, String payload, ContentType contentType, Class<T> clazz, String resourceName) {
        return execute(HttpMethod.POST, url, payload, contentType, clazz, "Create " + resourceName);
    }

    public <T> List<T> createList(String url, String payload, ContentType contentType, Class<T> clazz, String resourceName) {
        try {
            String response = sendRequest(buildRequest(HttpMethod.POST, url, payload, contentType));
            return JsonUtils.fromJsonArray(response, clazz);
        } catch (URISyntaxException e) {
            throw uriError(resourceName, url, e);
        }
    }

    public <T> T postWithQuery(String baseUrl, Map<String, String> params, String payload,
                               Class<T> clazz, String operation) {
        String url = buildUrlWithParams(baseUrl, params);
        return execute(HttpMethod.POST, url, payload, ContentType.APPLICATION_JSON, clazz, operation);
    }

    public void update(String url, String payload, ContentType contentType) {
        execute(HttpMethod.PUT, url, payload, contentType, Void.class, "Update");
    }

    public boolean download(String url, String destPath){
        try {
            ClassicRequestBuilder requestBuilder = buildRequest(HttpMethod.GET, url, null, null);
            return downloadApi(requestBuilder, destPath);
        } catch (URISyntaxException e) {
            throw uriError("Download file", url, e);
        }
    }
    // Centralized Execution

    private <T> T execute(HttpMethod method, String url, String payload,
                          ContentType contentType, Class<T> clazz,
                          String operation) {
        try {
            String response = sendRequest(buildRequest(method, url, payload, contentType));
            log.debug("{} response: {}", operation, response);
            if (clazz == Void.class) return null;
            return JsonUtils.fromJson(response, clazz);
        } catch (URISyntaxException e) {
            throw uriError(operation, url, e);
        }
    }


    private <T> List<T> executeList(String url, Class<T> clazz, String operation) {
        try {
            String response = sendRequest(buildRequest(HttpMethod.GET, url, null, null));
            log.debug("{} response : {}", operation, response);
            return JsonUtils.fromJsonArray(response, clazz);
        } catch (URISyntaxException e) {
            throw uriError(operation, url, e);
        }
    }



    // Request Builder

    private ClassicRequestBuilder buildRequest(HttpMethod method, String url,
                                               String payload, ContentType contentType) throws URISyntaxException {
        URI uri = new URI(url);
        ClassicRequestBuilder builder = switch (method) {
            case GET -> ClassicRequestBuilder.get(uri);
            case POST -> ClassicRequestBuilder.post(uri);
            case PUT -> ClassicRequestBuilder.put(uri);
            case DELETE -> ClassicRequestBuilder.delete(uri);
        };

        builder.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        if (payload != null && contentType != null) {
            builder.addHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            builder.setEntity(new StringEntity(payload, contentType));
        }

        return builder;
    }

    // Helpers

    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        try {
            URIBuilder builder = new URIBuilder(baseUrl);
            params.forEach(builder::addParameter);
            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw uriError("Build URL with params", baseUrl, e);
        }
    }

    private String sendRequest(ClassicRequestBuilder requestBuilder) {
        return HttpRequestExecutor.sendRequest(httpClient, requestBuilder);
    }

    private boolean downloadApi(ClassicRequestBuilder requestBuilder, String destPath){
        return HttpRequestExecutor.downloadFile(httpClient, requestBuilder, destPath);
    }

    private RuntimeException uriError(String operation, String url, URISyntaxException e) {
        log.error("Invalid URL for {}: {}", operation, url, e);
        return new RuntimeException(e);
    }
}
