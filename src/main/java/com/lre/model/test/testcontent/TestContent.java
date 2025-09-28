package com.lre.model.test.testcontent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.commandline.CommandLine;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.lgdistribution.LGDistribution;
import com.lre.model.test.testcontent.monitorprofile.MonitorProfile;
import com.lre.model.test.testcontent.workloadtype.WorkloadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "Content", namespace = LRE_API_XMLNS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestContent {

    @JsonProperty("Controller")
    @JacksonXmlProperty(localName = "Controller", namespace = LRE_API_XMLNS)
    private String controller;

    @JsonProperty("WorkloadType")
    @JacksonXmlProperty(localName = "WorkloadType", namespace = LRE_API_XMLNS)
    private WorkloadType workloadType;

    @JsonProperty("LGDistribution")
    @JacksonXmlProperty(localName = "LGDistribution", namespace = LRE_API_XMLNS)
    private LGDistribution lgDistribution;

    @JsonProperty("MonitorProfile")
    @JacksonXmlElementWrapper(localName = "MonitorProfiles", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "MonitorProfile", namespace = LRE_API_XMLNS)
    private List<MonitorProfile> monitorProfiles;

    @JsonProperty("GlobalRTS")
    @JacksonXmlElementWrapper(localName = "GlobalRTS", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "RTS", namespace = LRE_API_XMLNS)
    private List<RTS> globalRts;

    @JsonProperty("GlobalCommandLine")
    @JacksonXmlElementWrapper(localName = "GlobalCommandLine", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "CommandLine", namespace = LRE_API_XMLNS)
    private List<CommandLine> globalCommandLines;

    @JsonProperty("Groups")
    @JacksonXmlElementWrapper(localName = "Groups", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Group", namespace = LRE_API_XMLNS)
    private List<Group> groups;


    // YAML file specific fields

    @JsonProperty("LgAmount")
    private Integer lgAmount;

    @JsonProperty("WorkloadTypeCode")
    private Integer workloadTypeCode;

    @JsonProperty("MonitorProfileId")
    private String monitorProfileId;


}
