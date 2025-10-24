package com.lre.model.test.testcontent.groups.rts.thinktime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.ThinkTimeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThinkTime {

    @JsonProperty("Type")
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private ThinkTimeType type = ThinkTimeType.IGNORE; // ignore, replay, modify, random

    @JsonProperty("MultiplyFactor")
    @JacksonXmlProperty(localName = "MultiplyFactor", namespace = LRE_API_XMLNS)
    private Double multiplyFactor;

    @JsonProperty("MinPercentage")
    @JacksonXmlProperty(localName = "MinPercentage", namespace = LRE_API_XMLNS)
    private Integer minPercentage;

    @JsonProperty("MaxPercentage")
    @JacksonXmlProperty(localName = "MaxPercentage", namespace = LRE_API_XMLNS)
    private Integer maxPercentage;

    @JsonProperty("LimitThinkTimeSeconds")
    @JacksonXmlProperty(localName = "LimitThinkTimeSeconds", namespace = LRE_API_XMLNS)
    private Integer limitThinkTimeSeconds;

}
