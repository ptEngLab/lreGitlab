package com.lre.model.test.testcontent.scheduler.action.stopvusers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "StopVusers", namespace = LRE_API_XMLNS)
public class StopVusers {

    @JsonProperty("Type")
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private SchedulerVusersType type = SchedulerVusersType.SIMULTANEOUSLY;

    @JsonProperty("Vusers")
    @JacksonXmlProperty(localName = "Vusers", namespace = LRE_API_XMLNS)
    private Integer vusers;

    @JsonProperty("Ramp")
    @JacksonXmlProperty(localName = "Ramp", namespace = LRE_API_XMLNS)
    private Ramp ramp;

    public void setVusersFromString(String vusers) {
        this.vusers = Integer.parseInt(vusers);
    }
}