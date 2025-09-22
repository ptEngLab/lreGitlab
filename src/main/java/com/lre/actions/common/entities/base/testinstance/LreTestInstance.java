package com.lre.actions.common.entities.base.testinstance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LreTestInstance {

    @JsonProperty("TestID")
    private int testId;

    @JsonProperty("TestSetID")
    private int testSetId;

    @JsonProperty("TestInstanceID")
    private int testInstanceId;
}
