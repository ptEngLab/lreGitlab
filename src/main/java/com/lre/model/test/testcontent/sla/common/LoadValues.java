package com.lre.model.test.testcontent.sla.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.lre.actions.helpers.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadValues {

    @JsonProperty("Betweens")
    @JacksonXmlElementWrapper(localName = "Betweens", namespace = LRE_API_XMLNS)
    @JacksonXmlProperty(localName = "Between", namespace = LRE_API_XMLNS)
    private List<Between> between;

    @JsonProperty("LessThan")
    @JacksonXmlProperty(localName = "LessThan", namespace = LRE_API_XMLNS)
    private Integer lessThan;

    @JsonProperty("GreaterThanOrEqual")
    @JacksonXmlProperty(localName = "GreaterThanOrEqual", namespace = LRE_API_XMLNS)
    private Integer greaterThanOrEqual;

}
