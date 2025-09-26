package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WorkloadVusersDistributionModeEnum {
    BY_NUMBER("by number"),
    BY_PERCENTAGE("by percentage");

    private final String value;

    WorkloadVusersDistributionModeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WorkloadVusersDistributionModeEnum fromValue(String value) {
        for (WorkloadVusersDistributionModeEnum mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) return mode;
        }
        throw new IllegalArgumentException("Unknown VusersDistributionMode: " + value);
    }
}
