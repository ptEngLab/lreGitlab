package com.lre.client.api.builder;

import static com.lre.common.constants.ConfigConstants.CLOUD_TEMPLATE_RESOURCE_NAME;

/**
 * Builder for cloud template-related URLs
 */
public record TemplateUrlBuilder(String lreApiUrl) {

    public String getCloudTemplateUrl() {
        return UrlUtils.path(lreApiUrl, CLOUD_TEMPLATE_RESOURCE_NAME);
    }

    public String getCloudTemplateByIdUrl(int id) {
        return UrlUtils.pathWithId(lreApiUrl, CLOUD_TEMPLATE_RESOURCE_NAME, id);
    }
}