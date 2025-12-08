package com.lre.model.test.testcontent.scheduler.action.startgroup;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.enums.SchedulerStartGroupType;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "StartGroup", namespace = LRE_API_XMLNS)
public class StartGroup {

    @JsonProperty("Type")
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private SchedulerStartGroupType type = SchedulerStartGroupType.IMMEDIATELY;

    @JsonProperty("TimeInterval")
    @JacksonXmlProperty(localName = "TimeInterval", namespace = LRE_API_XMLNS)
    private TimeInterval timeInterval;

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;
}