package com.lre.model.test.testcontent.groups;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.test.testcontent.groups.hosts.Host;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.groups.script.Script;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

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

    @JsonProperty("Script")
    @JacksonXmlProperty(localName = "Script", namespace = LRE_API_XMLNS)
    private Script script;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("CommandLine")
    @JacksonXmlProperty(localName = "CommandLine", namespace = LRE_API_XMLNS)
    private String commandLine;

    @JsonProperty("GlobalRTS")
    @JacksonXmlProperty(localName = "GlobalRTS", namespace = LRE_API_XMLNS)
    private String globalRTS;

    @JsonProperty("GlobalCommandLine")
    @JacksonXmlProperty(localName = "GlobalCommandLine", namespace = LRE_API_XMLNS)
    private String globalCommandLine;

    @JsonProperty("Hosts")
    @JacksonXmlElementWrapper(localName = "Hosts", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Host", namespace = LRE_API_XMLNS)
    private List<Host> hosts;

    @JsonProperty("RTS")
    @JacksonXmlProperty(localName = "RTS", namespace = LRE_API_XMLNS)
    private RTS rts;

    @JsonProperty("Scheduler")
    @JacksonXmlProperty(localName = "Scheduler", namespace = LRE_API_XMLNS)
    private Scheduler scheduler;

}
