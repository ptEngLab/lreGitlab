package com.lre.client.api.base;

import com.lre.client.api.builder.ApiUrlBuilderLre;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.JsonUtils;
import com.lre.core.http.HttpRequestExecutor;
import com.lre.model.auth.AuthenticationClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.lre.common.constants.ConfigConstants.*;

@Slf4j
public record AuthenticationService(CloseableHttpClient httpClient, ApiUrlBuilderLre urlBuilder) {

    public static final String CONTENT_TYPE_HEADER = ContentType.APPLICATION_JSON.getMimeType();

    public boolean login(String username, String password, boolean authenticateWithToken) {
        return authenticateWithToken
                ? loginWithToken(username, password)
                : loginWithBasicAuth(username, password);
    }


    private boolean loginWithToken(String username, String password) {
        String authUrl = urlBuilder.auth().getAuthUrl(LRE_AUTHENTICATE_WITH_TOKEN);
        String payload = JsonUtils.toJson(new AuthenticationClient(username, password));

        ClassicRequestBuilder request = ClassicRequestBuilder.post(authUrl)
                .addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_HEADER)
                .setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        HttpRequestExecutor.sendRequest(httpClient, request);
        log.debug("Authentication successful with token");
        loginToWebProject();
        return true;
    }


    private boolean loginWithBasicAuth(String username, String password) {
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password)
                .getBytes(StandardCharsets.UTF_8));

        String authUrl = urlBuilder.auth().getAuthUrl(LRE_AUTHENTICATE_WITH_USERNAME);

        ClassicRequestBuilder request = ClassicRequestBuilder.get(authUrl)
                .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        HttpRequestExecutor.sendRequest(httpClient, request);
        log.debug("Authentication successful with username/password");
        loginToWebProject();
        return true;
    }


    private void loginToWebProject() {
        String domain = urlBuilder.domain();
        String project = urlBuilder.project();

        try {
            URI uri = new URIBuilder(urlBuilder.auth().getWebLoginUrl())
                    .addParameter("domain", domain)
                    .addParameter("project", project)
                    .build();

            ClassicRequestBuilder request = ClassicRequestBuilder.get(uri)
                    .addHeader(HttpHeaders.ACCEPT, CONTENT_TYPE_HEADER);

            HttpRequestExecutor.sendRequest(httpClient, request);
            log.debug("Web login successful for project: {}/{}", domain, project);
        } catch (URISyntaxException e) {
            log.error("Invalid URL for web login", e);
            throw new LreException("Token authentication failed", e);
        }
    }

    public void logout() {
        String logoutUrl = urlBuilder.auth().getAuthUrl(LRE_LOGOUT);
        ClassicRequestBuilder request = ClassicRequestBuilder.get(logoutUrl)
                .addHeader(HttpHeaders.ACCEPT, CONTENT_TYPE_HEADER);

        HttpRequestExecutor.sendRequest(httpClient, request);
        log.debug("Logout successful");
    }
}