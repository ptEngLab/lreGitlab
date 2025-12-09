package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchedulerDurationType implements StringValueEnum{
    INDEFINITELY("indefinitely"),
    RUN_FOR("run for"),
    UNTIL_COMPLETION("until completion");

    private final String value;

    SchedulerDurationType(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static SchedulerDurationType fromValue(String value) {
        return StringValueEnum.fromValue(SchedulerDurationType.class, value);
    }
}
