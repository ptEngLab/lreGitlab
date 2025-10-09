package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RunState {
    UNDEFINED(""),
    INITIALIZING("Initializing"),
    RUNNING("Running"),
    STOPPING("Stopping"),
    BEFORE_COLLATING_RESULTS("Before Collating Results"),
    COLLATING_RESULTS("Collating Results"),
    BEFORE_CREATING_ANALYSIS_DATA("Before Creating Analysis Data"),
    PENDING_CREATING_ANALYSIS_DATA("Pending Creating Analysis Data"),
    CREATING_ANALYSIS_DATA("Creating Analysis Data"),
    FINISHED("Finished"),
    FAILED_COLlATING_RESULTS("Failed Collating Results"),
    FAILED_CREATING_ANALYSIS_DATA("Failed Creating Analysis Data"),
    CANCELED("Canceled"),
    RUN_FAILURE("Run Failure");

    private final String value;

    RunState(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RunState fromValue(String value) {
        for (RunState state : values()) {
            if (state.value.equalsIgnoreCase(value)) return state;
        }
        throw new IllegalArgumentException("Unknown RunState: " + value);
    }
}
