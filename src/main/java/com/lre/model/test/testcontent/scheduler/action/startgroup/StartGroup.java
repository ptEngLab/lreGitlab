package com.lre.model.test.testcontent.scheduler.action.startgroup;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.lre.model.enums.SchedulerStartGroupType;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "StartGroup", namespace = LRE_API_XMLNS)
public class StartGroup {

    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private SchedulerStartGroupType type = SchedulerStartGroupType.IMMEDIATELY;

    @JacksonXmlProperty(localName = "TimeInterval", namespace = LRE_API_XMLNS)
    private TimeInterval timeInterval;

    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;
}