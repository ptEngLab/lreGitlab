package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LogOptionsType implements StringValueEnum {
    ON_ERROR("on error"),
    ALWAYS("always");

    private final String value;

    LogOptionsType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LogOptionsType fromValue(String value) {
        return StringValueEnum.fromValue(LogOptionsType.class, value);
    }
}
