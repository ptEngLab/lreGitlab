package com.lre.model.testSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreTestSet {

    @JsonProperty("TestSetName")
    private String testSetName;

    @JsonProperty("TestSetComment")
    private String testSetComment;

    @JsonProperty("TestSetParentId")
    private int testSetParentId;

    @JsonProperty("TestSetID")
    private int testSetId;
}
