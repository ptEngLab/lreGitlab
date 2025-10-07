package com.lre.model.test.testcontent.sla.trt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.test.testcontent.sla.common.Thresholds;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("Threshold")
    @JacksonXmlProperty(localName = "Threshold", namespace = LRE_API_XMLNS)
    private Float threshold;

    @JsonProperty("Thresholds")
    @JacksonXmlProperty(localName = "Thresholds", namespace = LRE_API_XMLNS)
    private Thresholds thresholds;


}
