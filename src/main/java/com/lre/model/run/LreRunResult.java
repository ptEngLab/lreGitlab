package com.lre.model.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreRunResult {

    @JsonProperty("ID")
    private int id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("RunID")
    private int runId;

    @JsonProperty("Type")
    private String type;
}
