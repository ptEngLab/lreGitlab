package com.lre.model.test.testcontent.scheduler.action.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.SchedulerVusersType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class VusersAction {

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
        setVusers(Integer.parseInt(vusers));
    }

    public abstract void applyTo(Action.ActionBuilder builder);
}
