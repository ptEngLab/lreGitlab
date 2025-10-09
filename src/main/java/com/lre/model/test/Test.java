package com.lre.model.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.TestContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.CommonUtils.normalizePathWithSubject;
import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "Test", namespace = LRE_API_XMLNS)
public class Test {

    @JsonProperty("ID")
    @JacksonXmlProperty(localName = "ID", namespace = LRE_API_XMLNS)
    private Integer id;

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("CreatedBy")
    @JacksonXmlProperty(localName = "CreatedBy", namespace = LRE_API_XMLNS)
    private String createdBy;

    @JsonProperty("LastModified")
    @JacksonXmlProperty(localName = "LastModified", namespace = LRE_API_XMLNS)
    private String lastModified;

    @JsonProperty("TestFolderPath")
    @JacksonXmlProperty(localName = "TestFolderPath", namespace = LRE_API_XMLNS)
    private String testFolderPath;

    @JsonProperty("Content")
    @JacksonXmlProperty(localName = "Content", namespace = LRE_API_XMLNS)
    private TestContent content;

    public Test(String name, String testFolderPath, TestContent content) {
        this.name = name;
        this.testFolderPath = testFolderPath;
        this.content = content;
    }

    public void setTestFolderPath(String testFolderPath) {
        this.testFolderPath = normalizePathWithSubject(testFolderPath);
    }

    public void normalizeAfterDeserialization() {
        this.testFolderPath = normalizePathWithSubject(this.testFolderPath);
    }

}

