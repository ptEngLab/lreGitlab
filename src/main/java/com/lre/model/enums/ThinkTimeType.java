package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ThinkTimeType implements StringValueEnum{
    IGNORE("ignore"),
    REPLAY("replay"),
    MODIFY("modify"),
    RANDOM("random");

    private final String value;

    ThinkTimeType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ThinkTimeType fromValue(String value) {
        return StringValueEnum.fromValue(ThinkTimeType.class, value);
    }
}
