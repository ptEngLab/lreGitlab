package com.lre.actions.common.entities.base.testSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static com.lre.actions.helpers.ConfigConstants.DEFAULT_TEST_SET_FOLDER_NAME;

@Data
public class LreTestSetFolderCreateRequest {

    @JsonProperty("TestSetFolderName")
    private String testSetFolderName = DEFAULT_TEST_SET_FOLDER_NAME;

    @JsonProperty("Parent")
    private int parentId = 0;

}
