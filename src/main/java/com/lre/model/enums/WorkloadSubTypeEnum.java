package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorkloadSubTypeEnum {
    @JsonProperty("by test")
    BY_TEST("by test"),

    @JsonProperty("by group")
    BY_GROUP("by group");

    private final String value;

    WorkloadSubTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
