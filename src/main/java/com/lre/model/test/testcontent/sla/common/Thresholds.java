package com.lre.model.test.testcontent.sla.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Thresholds {

    @JsonProperty("LessThanThreshold")
    @JacksonXmlProperty(localName = "LessThanThreshold", namespace = LRE_API_XMLNS)
    private Float lessThanThreshold;

    @JsonProperty("BetweenThreshold")
    @JacksonXmlProperty(localName = "BetweenThreshold", namespace = LRE_API_XMLNS)
    private BetweenThreshold betweenThreshold = new BetweenThreshold();

    @JsonProperty("GreaterThanOrEqualThreshold")
    @JacksonXmlProperty(localName = "GreaterThanOrEqualThreshold", namespace = LRE_API_XMLNS)
    private Float greaterThanOrEqualThreshold;
}
