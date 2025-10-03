package com.lre.model.test.testcontent.workloadtype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.WorkloadSubTypeEnum;
import com.lre.model.enums.WorkloadTypeEnum;
import com.lre.model.enums.WorkloadVusersDistributionModeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadType {

    @JsonProperty("Type")
    @JacksonXmlProperty(localName = "Type", namespace = LRE_API_XMLNS)
    private WorkloadTypeEnum type;

    @JsonProperty("SubType")
    @JacksonXmlProperty(localName = "SubType", namespace = LRE_API_XMLNS)
    private WorkloadSubTypeEnum subType;

    @JsonProperty("VusersDistributionMode")
    @JacksonXmlProperty(localName = "VusersDistributionMode", namespace = LRE_API_XMLNS)
    private WorkloadVusersDistributionModeEnum vusersDistributionMode;

    /**
     * Map numeric user input to valid WorkloadType combinations.
     * If userInput is null, defaults are applied.
     */
    public static WorkloadType fromUserInput(Integer userInput) {
        if (userInput == null) {
            return new WorkloadType(WorkloadTypeEnum.BASIC, WorkloadSubTypeEnum.BY_TEST,
                    WorkloadVusersDistributionModeEnum.BY_NUMBER); // default BASIC/BY_TEST/BY_NUMBER
        }

        return switch (userInput) {
            case 1 -> new WorkloadType(WorkloadTypeEnum.BASIC, WorkloadSubTypeEnum.BY_TEST,
                    WorkloadVusersDistributionModeEnum.BY_NUMBER);
            case 2 -> new WorkloadType(WorkloadTypeEnum.BASIC, WorkloadSubTypeEnum.BY_TEST,
                    WorkloadVusersDistributionModeEnum.BY_PERCENTAGE);
            case 3 -> new WorkloadType(WorkloadTypeEnum.BASIC, WorkloadSubTypeEnum.BY_GROUP,
                    WorkloadVusersDistributionModeEnum.BY_NUMBER);
            case 4 -> new WorkloadType(WorkloadTypeEnum.REAL_WORLD, WorkloadSubTypeEnum.BY_TEST,
                    WorkloadVusersDistributionModeEnum.BY_NUMBER);
            case 5 -> new WorkloadType(WorkloadTypeEnum.REAL_WORLD, WorkloadSubTypeEnum.BY_TEST,
                    WorkloadVusersDistributionModeEnum.BY_PERCENTAGE);
            case 6 -> new WorkloadType(WorkloadTypeEnum.REAL_WORLD, WorkloadSubTypeEnum.BY_GROUP,
                    WorkloadVusersDistributionModeEnum.BY_NUMBER);
            case 7 -> new WorkloadType(WorkloadTypeEnum.GOAL_ORIENTED, null, null);
            default -> throw new IllegalArgumentException("Invalid WorkloadType code: " + userInput);
        };
    }


    @JsonIgnore
    public String getWorkloadTypeAsStr() {
        return String.format("%s %s", this.getType().getValue(), this.getSubType().getValue()).toLowerCase(Locale.ROOT);

    }


}
