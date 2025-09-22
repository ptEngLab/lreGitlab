package com.lre.actions.common.entities.base.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lre.actions.common.entities.base.test.content.Content;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class Test {
    private final String xmlns = LRE_API_XMLNS;

    @JsonProperty("ID")
    private int id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CreatedBy")
    private String createdBy;

    @JsonProperty("LastModified")
    private String LastModified;

    @JsonProperty("TestFolderPath")
    private String testFolderPath;

    @JsonProperty("Content")
    private Content content;

    public Test(String name, String testFolderPath, Content content){
        this(0, name, testFolderPath, null, null, content);
    }
}
