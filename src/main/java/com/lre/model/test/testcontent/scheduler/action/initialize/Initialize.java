package com.lre.model.test.testcontent.scheduler.action.initialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.enums.SchedulerInitializeType;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "Initialize", namespace = LRE_API_XMLNS)
public class Initialize {

    @JsonProperty("Type")
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private SchedulerInitializeType type = SchedulerInitializeType.JUST_BEFORE_VUSER_RUNS;

    @JsonProperty("TimeInterval")
    @JacksonXmlProperty(localName = "TimeInterval", namespace = LRE_API_XMLNS)
    private TimeInterval timeInterval;

    @JsonProperty("Vusers")
    @JacksonXmlProperty(localName = "Vusers", namespace = LRE_API_XMLNS)
    private Integer vusers;

    @JsonProperty("WaitAfterInit")
    @JacksonXmlProperty(localName = "WaitAfterInit", namespace = LRE_API_XMLNS)
    private TimeInterval waitAfterInit;

    public void setVusersFromString(String vusers) {
        this.vusers = Integer.parseInt(vusers);
    }


}