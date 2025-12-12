package com.lre.client.api.builder;

import static com.lre.common.constants.ConfigConstants.HOST_RESOURCE_API;

/**
 * Builder for host-related URLs
 */
public record HostUrlBuilder(String lreApiUrl) {

    public String getHostsUrl() {
        return UrlUtils.path(lreApiUrl, HOST_RESOURCE_API);
    }
    
    public String getControllersUrl() {
        return UrlUtils.withComplexQuery(
            getHostsUrl(), 
            "{Purpose['*Controller*'];State['Operational']}"
        );
    }
    
    public String getLoadGeneratorsUrl() {
        return UrlUtils.withComplexQuery(
            getHostsUrl(),
            "{Purpose['*Load Generator*'];State['Operational']}"
        );
    }
}