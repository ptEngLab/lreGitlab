package com.lre.model.test.testcontent.scheduler.action.duration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.actions.utils.ConfigConstants;
import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "Duration", namespace = LRE_API_XMLNS)
public class Duration {

    @JsonProperty("Type")
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private SchedulerDurationType type = SchedulerDurationType.UNTIL_COMPLETION;

    @JsonProperty("TimeInterval")
    @JacksonXmlProperty(localName = "TimeInterval", namespace = ConfigConstants.LRE_API_XMLNS)
    private TimeInterval timeInterval;

}