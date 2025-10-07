package com.lre.model.test.testcontent.sla.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetweenThreshold {

    @JsonProperty("Threshold")
    @JacksonXmlProperty(localName = "Threshold", namespace = LRE_API_XMLNS)
    private List<Float> threshold;

}
