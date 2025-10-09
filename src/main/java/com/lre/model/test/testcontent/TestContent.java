package com.lre.model.test.testcontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.analysistemplate.AnalysisTemplate;
import com.lre.model.test.testcontent.automatictrending.AutomaticTrending;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.commandline.CommandLine;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.lgdistribution.LGDistribution;
import com.lre.model.test.testcontent.monitorofw.MonitorOFW;
import com.lre.model.test.testcontent.monitorprofile.MonitorProfile;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.sla.SLA;
import com.lre.model.test.testcontent.workloadtype.WorkloadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "Content", namespace = LRE_API_XMLNS)
@JsonInclude(JsonInclude.Include.NON_EMPTY) // skip null or empty lists in XML
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

    @JsonProperty("MonitorProfiles")
    @JacksonXmlElementWrapper(localName = "MonitorProfiles", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "MonitorProfile", namespace = LRE_API_XMLNS)
    private List<MonitorProfile> monitorProfiles = new ArrayList<>();

    @JsonProperty("MonitorsOFW")
    @JacksonXmlElementWrapper(localName = "MonitorsOFW", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "MonitorOFW", namespace = LRE_API_XMLNS)
    private List<MonitorOFW> monitorOFWIds = new ArrayList<>();

    @JsonProperty("GlobalRTSs")
    @JacksonXmlElementWrapper(localName = "GlobalRTS", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "RTS", namespace = LRE_API_XMLNS)
    private List<RTS> globalRts;

    @JsonProperty("GlobalCommandLines")
    @JacksonXmlElementWrapper(localName = "GlobalCommandLine", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "CommandLine", namespace = LRE_API_XMLNS)
    private List<CommandLine> globalCommandLines;

    @JsonProperty("Groups")
    @JacksonXmlElementWrapper(localName = "Groups", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Group", namespace = LRE_API_XMLNS)
    private List<Group> groups = new ArrayList<>();

    @JsonProperty("Scheduler")
    @JacksonXmlProperty(localName = "Scheduler", namespace = LRE_API_XMLNS)
    private Scheduler scheduler;

    @JsonProperty("SLA")
    @JacksonXmlProperty(localName = "SLA", namespace = LRE_API_XMLNS)
    private SLA sla;

    @JsonProperty("AnalysisTemplate")
    @JacksonXmlProperty(localName = "AnalysisTemplate", namespace = LRE_API_XMLNS)
    private AnalysisTemplate analysisTemplate;

    @JsonProperty("AutomaticTrending")
    @JacksonXmlProperty(localName = "AutomaticTrending", namespace = LRE_API_XMLNS)
    private AutomaticTrending automaticTrending;

}
