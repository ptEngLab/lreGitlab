package com.lre.model.test.testcontent.groups.rts.javavm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "JavaVm", namespace = LRE_API_XMLNS)
public class JavaVM {

    @JsonProperty("JavaEnvClassPaths")
    @JacksonXmlElementWrapper(localName = "JavaEnvClassPaths", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "JavaEnvClassPath", namespace = LRE_API_XMLNS)
    private List<String> javaEnvClassPaths;

    @JsonProperty("UserSpecifiedJdk")
    @JacksonXmlProperty(localName = "UserSpecifiedJdk", namespace = LRE_API_XMLNS)
    private Boolean userSpecifiedJdk;

    @JsonProperty("JdkHome")
    @JacksonXmlProperty(localName = "JdkHome", namespace = LRE_API_XMLNS)
    private String jdkHome;

    @JsonProperty("JavaVmParameters")
    @JacksonXmlProperty(localName = "JavaVmParameters", namespace = LRE_API_XMLNS)
    private String javaVmParameters;

    @JsonProperty("UseXboot")
    @JacksonXmlProperty(localName = "UseXboot", namespace = LRE_API_XMLNS)
    private Boolean useXboot;

    @JsonProperty("EnableClassLoaderPerVuser")
    @JacksonXmlProperty(localName = "EnableClassLoaderPerVuser", namespace = LRE_API_XMLNS)
    private Boolean enableClassLoaderPerVuser;
}
