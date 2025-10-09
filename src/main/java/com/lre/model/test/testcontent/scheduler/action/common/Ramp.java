package com.lre.model.test.testcontent.scheduler.action.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ramp {
    @JsonProperty("Vusers")
    @JacksonXmlProperty(localName = "Vusers", namespace = LRE_API_XMLNS)
    private Integer vusers;

    @JsonProperty("TimeInterval")
    @JacksonXmlProperty(localName = "TimeInterval", namespace = LRE_API_XMLNS)
    private TimeInterval timeInterval;

    public void setVusersFromString(String vusers) {
        if (vusers != null && vusers.matches("\\d+")) this.vusers = Integer.parseInt(vusers);
        else throw new IllegalArgumentException("Invalid vusers value: " + vusers);
    }


    public Ramp(String vusers, String timeInterval) {
        setVusersFromString(vusers);
        this.setTimeInterval(TimeInterval.parseTimeInterval(timeInterval));
    }
}
