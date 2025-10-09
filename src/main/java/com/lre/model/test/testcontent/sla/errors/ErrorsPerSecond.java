package com.lre.model.test.testcontent.sla.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.SlaLoadCriteria;
import com.lre.model.test.testcontent.sla.common.LoadValues;
import com.lre.model.test.testcontent.sla.common.Thresholds;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorsPerSecond {
    @JsonProperty("LoadCriterion")
    @JacksonXmlProperty(localName = "LoadCriterion", namespace = LRE_API_XMLNS)
    private SlaLoadCriteria loadCriterion;

    @JsonProperty("LoadValues")
    @JacksonXmlProperty(localName = "LoadValues", namespace = LRE_API_XMLNS)
    private LoadValues loadValues;

    @JsonProperty("Thresholds")
    @JacksonXmlProperty(localName = "Thresholds", namespace = LRE_API_XMLNS)
    private Thresholds thresholds;
}
