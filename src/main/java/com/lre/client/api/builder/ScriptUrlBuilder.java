package com.lre.client.api.builder;

import static com.lre.common.constants.ConfigConstants.SCRIPTS_RESOURCE_NAME;

/**
 * Builder for script-related URLs
 */
public record ScriptUrlBuilder(String lreApiUrl) {

    public String getScriptsUrl() {
        return UrlUtils.path(lreApiUrl, SCRIPTS_RESOURCE_NAME);
    }

    public String getScriptByIdUrl(int scriptId) {
        return UrlUtils.pathWithId(lreApiUrl, SCRIPTS_RESOURCE_NAME, scriptId);
    }

    public String getUploadScriptUrl() {
        return getScriptsUrl(); // Same as scripts URL for uploads
    }

    public String getDeleteScriptUrl(int scriptId) {
        return getScriptByIdUrl(scriptId); // Same as script by ID for deletes
    }
}