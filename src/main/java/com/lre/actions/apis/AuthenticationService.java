package com.lre.actions.apis;

import com.lre.model.auth.AuthenticationClient;
import com.lre.actions.httpclient.HttpRequestExecutor;
import com.lre.actions.utils.JsonUtils;
import lombok.AllArgsConstructor;
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

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
@AllArgsConstructor
public class AuthenticationService {
    private final CloseableHttpClient httpClient;
    private final ApiUrlBuilder urlBuilder;

    public boolean login(String username, String password, boolean authenticateWithToken) {
        return authenticateWithToken
                ? loginWithToken(username, password)
                : loginWithBasicAuth(username, password);
    }


    private boolean loginWithToken(String username, String password) {
        String authUrl = urlBuilder.getAuthUrl(LRE_AUTHENTICATE_WITH_TOKEN);
        String payload = JsonUtils.toJson(new AuthenticationClient(username, password));

        ClassicRequestBuilder request = ClassicRequestBuilder.post(authUrl)
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        HttpRequestExecutor.sendRequest(httpClient, request);
        log.debug("Authentication successful with token");
        loginToWebProject();
        return true;
    }


    private boolean loginWithBasicAuth(String username, String password) {
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password)
                .getBytes(StandardCharsets.UTF_8));

        String authUrl = urlBuilder.getAuthUrl(LRE_AUTHENTICATE_WITH_USERNAME);

        ClassicRequestBuilder request = ClassicRequestBuilder.get(authUrl)
                .addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        HttpRequestExecutor.sendRequest(httpClient, request);
        log.debug("Authentication successful with username/password");
        loginToWebProject();
        return true;
    }


    private void loginToWebProject() {
        try {
            URI uri = new URIBuilder(urlBuilder.getWebLoginUrl())
                    .addParameter("domain", urlBuilder.getDomain())
                    .addParameter("project", urlBuilder.getProject())
                    .build();

            ClassicRequestBuilder request = ClassicRequestBuilder.get(uri)
                    .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

            HttpRequestExecutor.sendRequest(httpClient, request);
            log.debug("Web login successful for project: {}/{}", urlBuilder.getDomain(), urlBuilder.getProject());
        } catch (URISyntaxException e) {
            log.error("Invalid URL for web login", e);
            throw new RuntimeException(e);
        }
    }

    public void logout() {
        String logoutUrl = urlBuilder.getAuthUrl(LRE_LOGOUT);
        ClassicRequestBuilder request = ClassicRequestBuilder.get(logoutUrl)
                .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        HttpRequestExecutor.sendRequest(httpClient, request);
        log.debug("Logout successful");
    }
}