package com.lre.model.testplan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreTestPlan {

    @JsonProperty("Id")
    private int id;

    @JsonProperty("ParentId")
    private int parentId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("FullPath")
    private String fullPath;
}
