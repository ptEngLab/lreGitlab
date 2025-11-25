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

        boolean syncGitlab = operation == Operation.SYNC_GITLAB_WITH_LRE;
        boolean sendEmail = operation == Operation.SEND_EMAIL;
        boolean requiresRunId = operation == Operation.EXTRACT_RESULTS;

        // -------------------------
        // Second pass: conditional parameters
        // -------------------------
        for (ParameterDefinitions.ConfigParameter<?> def : definitions) {
            if (def.conditional()) {
                boolean required = isRequired(def, sendEmail, syncGitlab, requiresRunId);

                parseAndPut(params, def, required);
            }
        }

        log.debug("Completed parameter parsing");
        return params;
    }

    private static boolean isRequired(ParameterDefinitions.ConfigParameter<?> def,
                                      boolean sendEmail,
                                      boolean syncGitlab,
                                      boolean requiresRunId) {

        return switch (def.key()) {
            case ParameterDefinitions.Keys.EMAIL_TO -> sendEmail;               // Email logic
            case ParameterDefinitions.Keys.GITLAB_TOKEN,
                 ParameterDefinitions.Keys.GITLAB_PROJECT_ID -> syncGitlab;     // GitLab logic
            case ParameterDefinitions.Keys.LRE_RUN_ID -> requiresRunId;         // Run ID required for EXTRACT_RESULTS
            default -> def.required();                                          // Everything else follows definition
        };
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
