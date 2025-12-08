package com.lre.model.test.testcontent.workloadtype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.StringValueEnum;
import com.lre.model.enums.WorkloadSubType;
import com.lre.model.enums.WorkloadTypeEnum;
import com.lre.model.enums.WorkloadVusersDistributionMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;
import static com.lre.common.utils.CommonUtils.toTitleCase;
import static com.lre.model.enums.WorkloadTypeEnum.BASIC;
import static com.lre.model.enums.WorkloadTypeEnum.REAL_WORLD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadType {

    @JsonProperty("Type")
    @JacksonXmlProperty(localName = "Type", namespace = LRE_API_XMLNS)
    private WorkloadTypeEnum type;

    @JsonProperty("SubType")
    @JacksonXmlProperty(localName = "SubType", namespace = LRE_API_XMLNS)
    private WorkloadSubType subType;

    @JsonProperty("VusersDistributionMode")
    @JacksonXmlProperty(localName = "VusersDistributionMode", namespace = LRE_API_XMLNS)
    private WorkloadVusersDistributionMode vusersDistributionMode;

    public WorkloadType(WorkloadTypeEnum type, WorkloadSubType subType) {
        this.type = type;
        this.subType = subType;
    }

    public WorkloadType(WorkloadTypeEnum type) {
        this.type = type;
    }

    /**
     * Map numeric user input to valid WorkloadType combinations.
     * If userInput is null, defaults are applied.
     */
    public static WorkloadType fromUserInput(Integer userInput) {
        if (userInput == null) {
            return new WorkloadType(BASIC, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_NUMBER); // default BASIC/BY_TEST/BY_NUMBER
        }

        return switch (userInput) {
            case 1 -> new WorkloadType(BASIC, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_NUMBER);
            case 2 -> new WorkloadType(BASIC, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_PERCENTAGE);
            case 3 -> new WorkloadType(BASIC, WorkloadSubType.BY_GROUP);
            case 4 -> new WorkloadType(REAL_WORLD, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_NUMBER);
            case 5 ->
                    new WorkloadType(REAL_WORLD, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_PERCENTAGE);
            case 6 -> new WorkloadType(REAL_WORLD, WorkloadSubType.BY_GROUP);
            case 7 -> new WorkloadType(WorkloadTypeEnum.GOAL_ORIENTED);
            default -> throw new IllegalArgumentException("Invalid WorkloadType code: " + userInput);
        };
    }


    @JsonIgnore
    public String getWorkloadTypeAsStr() {
        String type = safeValue(getType());
        String subType = safeValue(getSubType());
        return String.format("%s %s", type, subType).trim().toLowerCase(Locale.ROOT);
    }


    @JsonIgnore
    public String getFullWorkloadTypeAsStr() {
        String type = safeValue(getType());
        String subType = safeValue(getSubType());
        String mode = (getVusersDistributionMode() != null) ? getVusersDistributionMode().getValue() : null;

        String raw = StringUtils.isBlank(mode)
                ? String.format("%s %s", type, subType)
                : String.format("%s %s (%s)", type, subType, mode);

        return toTitleCase(raw);
    }

    private String safeValue(StringValueEnum e) {
        return (e == null || e.getValue() == null) ? "" : e.getValue();
    }
}
