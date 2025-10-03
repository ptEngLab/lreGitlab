package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchedulerInitializeType {
    GRADUALLY("gradually"),
    JUST_BEFORE_VUSER_RUNS("just before vuser runs"),
    SIMULTANEOUSLY("simultaneously");

    private final String value;

    SchedulerInitializeType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SchedulerInitializeType fromValue(String value) {
        for (SchedulerInitializeType type : SchedulerInitializeType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown InitializeType: " + value);
    }
}
