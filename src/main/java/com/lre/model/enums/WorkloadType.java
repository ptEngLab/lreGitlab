package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WorkloadType {
    BASIC("basic"),
    REAL_WORLD("real-world"),
    GOAL_ORIENTED("goal oriented");

    private final String value;

    WorkloadType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WorkloadType fromValue(String value) {
        for (WorkloadType type : values()) {
            if (type.value.equalsIgnoreCase(value)) return type;
        }
        throw new IllegalArgumentException("Unknown WorkloadTypeEnum: " + value);
    }
}
