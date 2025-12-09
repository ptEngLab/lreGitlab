package com.lre.model.test.testcontent.scheduler.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.initialize.Initialize;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JacksonXmlRootElement(localName = "Action", namespace = LRE_API_XMLNS)
public class Action {

    @JsonProperty("Initialize")
    @JacksonXmlProperty(localName = "Initialize", namespace = LRE_API_XMLNS)
    private Initialize initialize;

    @JsonProperty("StartVusers")
    @JacksonXmlProperty(localName = "StartVusers", namespace = LRE_API_XMLNS)
    private StartVusers startVusers;

    @JsonProperty("StopVusers")
    @JacksonXmlProperty(localName = "StopVusers", namespace = LRE_API_XMLNS)
    private StopVusers stopVusers;

    @JsonProperty("Duration")
    @JacksonXmlProperty(localName = "Duration", namespace = LRE_API_XMLNS)
    private Duration duration;

    @JsonProperty("StartGroup")
    @JacksonXmlProperty(localName = "StartGroup", namespace = LRE_API_XMLNS)
    private StartGroup startGroup;


}
