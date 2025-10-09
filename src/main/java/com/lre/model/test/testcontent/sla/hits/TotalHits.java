package com.lre.model.test.testcontent.sla.hits;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalHits {
    @JsonProperty("Threshold")
    @JacksonXmlProperty(localName = "Threshold", namespace = LRE_API_XMLNS)
    private Float threshold;
}
