package com.lre.model.test.testcontent.groups.rts.jmeter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JMeter {

    @JsonProperty("JREPath")
    @JacksonXmlProperty(localName = "JREPath", namespace = LRE_API_XMLNS)
    private String jrePath;

    @JsonProperty("AdditionalParameters")
    @JacksonXmlProperty(localName = "AdditionalParameters", namespace = LRE_API_XMLNS)
    private String additionalParameters;

    @JsonProperty("StartMeasurements")
    @JacksonXmlProperty(localName = "StartMeasurements", namespace = LRE_API_XMLNS)
    private Boolean startMeasurements;

    @JsonProperty("JMeterHomePath")
    @JacksonXmlProperty(localName = "JMeterHomePath", namespace = LRE_API_XMLNS)
    private String jMeterHomePath;

    @JsonProperty("JMeterUseDefaultPort")
    @JacksonXmlProperty(localName = "JMeterUseDefaultPort", namespace = LRE_API_XMLNS)
    private Boolean jMeterUseDefaultPort;

    @JsonProperty("JMeterMinPort")
    @JacksonXmlProperty(localName = "JMeterMinPort", namespace = LRE_API_XMLNS)
    private Integer jMeterMinPort;

    @JsonProperty("JMeterMaxPort")
    @JacksonXmlProperty(localName = "JMeterMaxPort", namespace = LRE_API_XMLNS)
    private Integer jMeterMaxPort;
}
