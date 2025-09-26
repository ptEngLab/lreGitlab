package com.lre.model.testplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LreTestPlanCreationRequest {

    @JsonProperty("Path")
    private String path;

    @JsonProperty("Name")
    private String name;
}
