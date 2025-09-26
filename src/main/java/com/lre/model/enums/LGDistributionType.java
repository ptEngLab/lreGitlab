package com.lre.model.enums;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum LGDistributionType {
    ALL_TO_EACH_GROUP("all to each group"),
    MANUAL("manual");

    private final String value;

    LGDistributionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LGDistributionType fromValue(String value) {
        for (LGDistributionType type : values()) {
            if (type.value.equalsIgnoreCase(value)) return type;
        }
        throw new IllegalArgumentException("Unknown LGDistributionType: " + value);
    }
}
