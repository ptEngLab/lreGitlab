package com.lre.core.config;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public record ConfigValidator(ParameterResolver resolver) {

    public void validate(Map<String, Object> params) {
        validateRequiredParameters();
        validateParameterValues(params);
    }

    private void validateRequiredParameters() {
        List<String> missingRequired = resolver.getMissingRequiredParams();
        if (!missingRequired.isEmpty()) {
            handleTimeslotParameters(missingRequired);

            List<String> critical = new ArrayList<>(missingRequired);
            critical.remove(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_HOURS.toUpperCase());
            critical.remove(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_MINUTES.toUpperCase());

            if (!critical.isEmpty()) {
                log.error("Missing critical parameters: {}", String.join(", ", critical));
                throw new IllegalArgumentException("Missing critical parameters: " + String.join(", ", critical));
            }
        }
    }

    private void handleTimeslotParameters(List<String> missingRequired) {
        boolean missingHours = missingRequired.contains(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_HOURS.toUpperCase());
        boolean missingMinutes = missingRequired.contains(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_MINUTES.toUpperCase());

        if (missingHours || missingMinutes) {
            log.warn("Timeslot duration parameters are missing, defaulting to 30 minutes.");
        }
    }

    private void validateParameterValues(Map<String, Object> params) {
        validateTimeslotParameters(params);
    }

    private void validateTimeslotParameters(Map<String, Object> params) {
        Integer hours = (Integer) params.get(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_HOURS);
        Integer minutes = (Integer) params.get(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_MINUTES);

        if (hours < 0 || minutes < 0 || minutes >= 60) {
            log.warn("Invalid timeslot duration: {} hours {} minutes. Using defaults.", hours, minutes);
            params.put(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_HOURS, ParameterDefinitions.Defaults.LRE_TIMESLOT_DURATION_HOURS);
            params.put(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_MINUTES, ParameterDefinitions.Defaults.LRE_TIMESLOT_DURATION_MINUTES);
        }
    }
}