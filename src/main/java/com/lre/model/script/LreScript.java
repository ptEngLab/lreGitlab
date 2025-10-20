package com.lre.model.script;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreScript {

    @JsonProperty("ID")
    private int id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CreatedBy")
    private String createdBy;

    @JsonProperty("TestFolderPath")
    private String testFolderPath;

    @JsonProperty("WorkingMode")
    private String workingMode;

    @JsonProperty("Protocol")
    private String protocol;

    @JsonProperty("LastModifyDate")
    private String lastModifyDate;

    @JsonProperty("CreationDate")
    private String creationDate;

    @JsonProperty("IsScriptLocked")
    private boolean isScriptLocked;

    @JsonProperty("SplitScriptResponse")
    private String splitScriptResponse;

}
