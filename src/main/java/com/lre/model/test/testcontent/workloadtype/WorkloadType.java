package com.lre.model.test.testcontent.workloadtype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.WorkloadSubType;
import com.lre.model.enums.WorkloadVusersDistributionMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadType {

    @JsonProperty("Type")
    @JacksonXmlProperty(localName = "Type", namespace = LRE_API_XMLNS)
    private com.lre.model.enums.WorkloadType type;

    @JsonProperty("SubType")
    @JacksonXmlProperty(localName = "SubType", namespace = LRE_API_XMLNS)
    private WorkloadSubType subType;

    @JsonProperty("VusersDistributionMode")
    @JacksonXmlProperty(localName = "VusersDistributionMode", namespace = LRE_API_XMLNS)
    private WorkloadVusersDistributionMode vusersDistributionMode;

    public WorkloadType(com.lre.model.enums.WorkloadType type, WorkloadSubType subType) {
        this.type = type;
        this.subType = subType;
    }

    public WorkloadType(com.lre.model.enums.WorkloadType type) {
        this.type = type;
    }

    /**
     * Map numeric user input to valid WorkloadType combinations.
     * If userInput is null, defaults are applied.
     */
    public static WorkloadType fromUserInput(Integer userInput) {
        if (userInput == null) {
            return new WorkloadType(com.lre.model.enums.WorkloadType.BASIC, WorkloadSubType.BY_TEST,
                    WorkloadVusersDistributionMode.BY_NUMBER); // default BASIC/BY_TEST/BY_NUMBER
        }

        return switch (userInput) {
            case 1 -> new WorkloadType(com.lre.model.enums.WorkloadType.BASIC, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_NUMBER);
            case 2 -> new WorkloadType(com.lre.model.enums.WorkloadType.BASIC, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_PERCENTAGE);
            case 3 -> new WorkloadType(com.lre.model.enums.WorkloadType.BASIC, WorkloadSubType.BY_GROUP);
            case 4 -> new WorkloadType(com.lre.model.enums.WorkloadType.REAL_WORLD, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_NUMBER);
            case 5 -> new WorkloadType(com.lre.model.enums.WorkloadType.REAL_WORLD, WorkloadSubType.BY_TEST, WorkloadVusersDistributionMode.BY_PERCENTAGE);
            case 6 -> new WorkloadType(com.lre.model.enums.WorkloadType.REAL_WORLD, WorkloadSubType.BY_GROUP);
            case 7 -> new WorkloadType(com.lre.model.enums.WorkloadType.GOAL_ORIENTED);
            default -> throw new IllegalArgumentException("Invalid WorkloadType code: " + userInput);
        };
    }


    @JsonIgnore
    public String getWorkloadTypeAsStr() {
        return String.format("%s %s", this.getType().getValue(), this.getSubType().getValue()).toLowerCase(Locale.ROOT);

    }

}
