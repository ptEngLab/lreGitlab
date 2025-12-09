package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WorkloadVusersDistributionMode implements StringValueEnum{
    BY_NUMBER("by number"),
    BY_PERCENTAGE("by percentage");

    private final String value;

    WorkloadVusersDistributionMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WorkloadVusersDistributionMode fromValue(String value) {
        return StringValueEnum.fromValue(WorkloadVusersDistributionMode.class, value);
    }
}
