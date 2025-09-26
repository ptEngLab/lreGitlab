package com.lre.model.testSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreTestSetFolder {

    @JsonProperty("TestSetFolderName")
    private String testSetFolderName;

    @JsonProperty("Parent")
    private int parentId;

    @JsonProperty("TestSetFolderId")
    private int testSetFolderId;

}
