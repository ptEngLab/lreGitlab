package com.lre.actions.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.lre.actions.helpers.CommonMethods.convertToType;

public class ParameterResolver {
    private final JsonNode configContent;
    private final List<String> missingRequiredParams;

    public ParameterResolver(JsonNode configContent) {
        this.configContent = configContent;
        this.missingRequiredParams = new ArrayList<>();
    }

    public <T> T getParameterValue(String parameterKey, boolean isRequired, T defaultValue) {
        if (StringUtils.isBlank(parameterKey)) return defaultValue;

        // Normalize for ENV
        String envKey = parameterKey.toUpperCase();
        String parameterValue = System.getenv(envKey);

        // Fall back to config.json
        if (StringUtils.isBlank(parameterValue)) {
            JsonNode node = configContent.get(parameterKey);
            if (node != null && !node.isNull()) {
                parameterValue = node.asText();
            }
        }

        // If found, try to convert
        if (StringUtils.isNotBlank(parameterValue)) {
            return convertToType(parameterValue.trim(), defaultValue);
        }

        // Track missing required param
        if (isRequired) {
            missingRequiredParams.add(parameterKey);
        }

        return defaultValue;
    }

    public List<String> getMissingRequiredParams() {
        return new ArrayList<>(missingRequiredParams);
    }
}
