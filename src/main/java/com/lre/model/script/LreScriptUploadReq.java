package com.lre.model.script;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreScriptUploadReq {
    @JsonProperty("TestFolderPath")
    private String testFolderPath;

    @JsonProperty("Overwrite")
    private boolean overwrite = true;

    @JsonProperty("RuntimeOnly")
    private boolean runtimeOnly = true;

    @JsonProperty("KeepCheckedOut")
    private boolean keepCheckedOut = false;

    public LreScriptUploadReq(String testFolderPath) {
        this.testFolderPath = testFolderPath;
    }
}
