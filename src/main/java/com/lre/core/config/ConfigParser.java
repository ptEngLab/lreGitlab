package com.lre.core.config;

import com.lre.model.enums.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public record ConfigParser(ParameterResolver resolver, Operation operation) {

    public Map<String, Object> parseParameters(List<ParameterDefinitions.ConfigParameter<?>> definitions) {
        Map<String, Object> params = new HashMap<>();
        log.debug("Starting parameter parsing");

        // -------------------------
        // First pass: non-conditional parameters
        // -------------------------
        for (ParameterDefinitions.ConfigParameter<?> def : definitions) {
            if (!def.conditional()) {
                parseAndPut(params, def, def.required());
            }
        }

        // -------------------------
        // Evaluate conditional flags (after first pass)
        // -------------------------
        boolean syncGitlab = Boolean.TRUE.equals(params.get(ParameterDefinitions.Keys.SYNC_GITLAB_WITH_LRE_FLAG));
        boolean sendEmail = Boolean.TRUE.equals(params.get(ParameterDefinitions.Keys.SEND_EMAIL_FLAG));

        if(operation == Operation.SYNC_GITLAB_WITH_LRE) syncGitlab = true;
        if(operation == Operation.SEND_EMAIL) sendEmail = true;

        // -------------------------
        // Second pass: conditional parameters
        // -------------------------
        for (ParameterDefinitions.ConfigParameter<?> def : definitions) {
            if (def.conditional()) {
                boolean required = isRequired(def, sendEmail, syncGitlab);

                parseAndPut(params, def, required);
            }
        }

        log.debug("Completed parameter parsing");
        return params;
    }

    private static boolean isRequired(ParameterDefinitions.ConfigParameter<?> def, boolean sendEmail, boolean syncGitlab) {
        if (def.key().equals(ParameterDefinitions.Keys.EMAIL_TO)) {
            return sendEmail;
        }
        if (def.key().equals(ParameterDefinitions.Keys.GITLAB_TOKEN)) {
            return syncGitlab;
        }
        return def.required();
    }


    private <T> void parseAndPut(Map<String, Object> map, ParameterDefinitions.ConfigParameter<T> def, boolean required) {
        String value = resolver.getParameterValue(def.key().toUpperCase(), required, String.valueOf(def.defaultValue()));

        if (value == null) {
            map.put(def.key(), def.defaultValue());
            log.warn("Parameter '{}' is missing, using default: {}", def.key(), def.defaultValue());
            return;
        }

        try {
            if (def.defaultValue() instanceof Integer) {
                map.put(def.key(), Integer.parseInt(value));
            } else if (def.defaultValue() instanceof Boolean) {
                map.put(def.key(), Boolean.parseBoolean(value.toLowerCase()));
            } else if (def.defaultValue() instanceof Long) {
                map.put(def.key(), Long.parseLong(value.toLowerCase()));
            } else {
                map.put(def.key(), value);
            }
        } catch (NumberFormatException e) {
            map.put(def.key(), def.defaultValue());
            log.warn("Invalid numeric value '{}' for parameter '{}', using default: {}", value, def.key(), def.defaultValue());
        } catch (Exception e) {
            map.put(def.key(), def.defaultValue());
            log.warn("Unexpected error parsing parameter '{}', using default: {}", def.key(), def.defaultValue(), e);
        }
    }
}
