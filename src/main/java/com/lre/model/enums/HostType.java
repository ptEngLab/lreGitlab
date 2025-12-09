package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HostType implements StringValueEnum {
    SPECIFIC("specific"),
    AUTOMATCH("automatch"),
    CLOUD("cloud"),
    DYNAMIC("dynamic");

    private final String value;

    HostType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static HostType fromValue(String value) {
        return StringValueEnum.fromValue(HostType.class, value);
    }
}
