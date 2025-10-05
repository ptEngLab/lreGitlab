package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkloadSubType {
    @JsonProperty("by test")
    BY_TEST("by test"),

    @JsonProperty("by group")
    BY_GROUP("by group");

    private final String value;


    @JsonValue
    public String getValue() {
        return value;
    }

    WorkloadSubType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
