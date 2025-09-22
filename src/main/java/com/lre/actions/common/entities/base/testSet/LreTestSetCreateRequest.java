package com.lre.actions.common.entities.base.testSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.lre.actions.helpers.ConfigConstants.DEFAULT_TEST_SET_NAME;

@Data
@AllArgsConstructor
public class LreTestSetCreateRequest {
    @JsonProperty("TestSetName")
    private String testSetName = DEFAULT_TEST_SET_NAME;

    @JsonProperty("TestSetComment")
    private String testSetComment = "auto test set";

    @JsonProperty("TestSetParentId")
    private int testSetParentId;

    public LreTestSetCreateRequest(int testSetParentId) {
        this.testSetParentId = testSetParentId;
    }

}
