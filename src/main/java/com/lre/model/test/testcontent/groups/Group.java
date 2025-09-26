package com.lre.model.test.testcontent.groups;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.test.testcontent.groups.hosts.Host;
import com.lre.model.test.testcontent.groups.script.Script;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("Vusers")
    @JacksonXmlProperty(localName = "Vusers", namespace = LRE_API_XMLNS)
    private Integer vusers;

    @JsonProperty("CommandLine")
    @JacksonXmlProperty(localName = "CommandLine", namespace = LRE_API_XMLNS)
    private String commandLine;

    @JsonProperty("GlobalRTS")
    @JacksonXmlProperty(localName = "GlobalRTS", namespace = LRE_API_XMLNS)
    private String globalRTS;

    @JsonProperty("GlobalCommandLine")
    @JacksonXmlProperty(localName = "GlobalCommandLine", namespace = LRE_API_XMLNS)
    private String globalCommandLine;

    @JsonProperty("Script")
    @JacksonXmlProperty(localName = "Script", namespace = LRE_API_XMLNS)
    private Script script;

    @JsonProperty("Hosts")
    @JacksonXmlElementWrapper(localName = "Hosts", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Host", namespace = LRE_API_XMLNS)
    private List<Host> hosts;


    // For YAML mapping
    @JsonProperty("ScriptId")
    private Integer scriptId = 0;

    @JsonProperty("ScriptName")
    private String scriptName;

    @JsonProperty("HostName")
    private String hostname;

    @JsonProperty("HostTemplate")
    private String hostTemplate;
}
