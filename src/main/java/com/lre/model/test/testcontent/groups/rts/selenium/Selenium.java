package com.lre.model.test.testcontent.groups.rts.selenium;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Selenium {

    @JsonProperty("JREPath")
    @JacksonXmlProperty(localName = "JREPath", namespace = LRE_API_XMLNS)
    private String jrePath;

    @JsonProperty("ClassPath")
    @JacksonXmlProperty(localName = "ClassPath", namespace = LRE_API_XMLNS)
    private String classPath;

    @JsonProperty("TestNgFiles")
    @JacksonXmlProperty(localName = "TestNgFiles", namespace = LRE_API_XMLNS)
    private String testNgFiles;
}
