package com.lre.model.test.testcontent.groups.script;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Script {

    @JsonProperty("ID")
    @JacksonXmlProperty(localName = "ID", namespace = LRE_API_XMLNS)
    private int id;

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("CreatedBy")
    @JacksonXmlProperty(localName = "CreatedBy", namespace = LRE_API_XMLNS)
    private String createdBy;

    @JsonProperty("TestFolderPath")
    @JacksonXmlProperty(localName = "TestFolderPath", namespace = LRE_API_XMLNS)
    private String testFolderPath;

    @JsonProperty("WorkingMode")
    @JacksonXmlProperty(localName = "WorkingMode", namespace = LRE_API_XMLNS)
    private String workingMode;

    @JsonProperty("Protocol")
    @JacksonXmlProperty(localName = "Protocol", namespace = LRE_API_XMLNS)
    private String protocol;

    @JsonProperty("LastModifyDate")
    @JacksonXmlProperty(localName = "LastModifyDate", namespace = LRE_API_XMLNS)
    private String lastModifyDate;

    @JsonProperty("CreationDate")
    @JacksonXmlProperty(localName = "CreationDate", namespace = LRE_API_XMLNS)
    private String creationDate;

    @JsonProperty("IsScriptLocked")
    @JacksonXmlProperty(localName = "IsScriptLocked", namespace = LRE_API_XMLNS)
    private Boolean isScriptLocked;

    @JsonProperty("SplitScriptResponse")
    @JacksonXmlProperty(localName = "SplitScriptResponse", namespace = LRE_API_XMLNS)
    private String splitScriptResponse;


    @JsonProperty("ProtocolType")
    @JacksonXmlProperty(localName = "ProtocolType", namespace = LRE_API_XMLNS)
    private String protocolType;


    public Script(int id, String protocolType){
        this.id = id;
        this.protocolType = protocolType;
    }
}
