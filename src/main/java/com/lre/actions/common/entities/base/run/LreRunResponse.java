package com.lre.actions.common.entities.base.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreRunResponse {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("TestId")
    private int testId;

    @JsonProperty("RunId")
    private int runId;

    @JsonProperty("QcRunId")
    private int qcRunId;

    @JsonProperty("URL")
    private String url;
}
