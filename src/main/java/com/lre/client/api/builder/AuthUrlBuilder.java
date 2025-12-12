package com.lre.client.api.builder;

import static com.lre.common.constants.ConfigConstants.LRE_WEB_LOGIN_TO_PROJECT;

/**
 * Builder for authentication-related URLs
 */
public record AuthUrlBuilder(String baseUrl, String lreWebUrl) {


    public String getAuthUrl(String endpoint) {
        return UrlUtils.path(baseUrl, endpoint);
    }

    public String getWebLoginUrl() {
        return UrlUtils.path(lreWebUrl, LRE_WEB_LOGIN_TO_PROJECT);
    }
}