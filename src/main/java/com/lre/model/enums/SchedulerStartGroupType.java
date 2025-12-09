package com.lre.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SchedulerStartGroupType implements StringValueEnum {
    IMMEDIATELY("immediately"),
    WITH_DELAY("with delay"),
    WHEN_GROUP_FINISHES("when group finishes");

    private final String value;

    SchedulerStartGroupType(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static SchedulerStartGroupType fromValue(String value) {
        return StringValueEnum.fromValue(SchedulerStartGroupType.class, value);
    }
}
