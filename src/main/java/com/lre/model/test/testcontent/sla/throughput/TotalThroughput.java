package com.lre.model.test.testcontent.sla.throughput;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalThroughput {

    @JsonProperty("Threshold")
    @JacksonXmlProperty(localName = "Threshold", namespace = LRE_API_XMLNS)
    private Integer threshold;
}
