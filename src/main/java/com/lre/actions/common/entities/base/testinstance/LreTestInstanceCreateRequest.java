package com.lre.actions.common.entities.base.testinstance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LreTestInstanceCreateRequest {

    @JsonProperty("TestID")
    private int testId;

    @JsonProperty("TestSetID")
    private int testSetId;
}
