package com.lre.model.test.testcontent.groups.rts.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.LogOptionsType;
import com.lre.model.enums.LogType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @JsonProperty("Type")
    @JacksonXmlProperty(isAttribute = true, localName = "Type")
    private LogType type = LogType.STANDARD; // ignore, standard, extended

    @JsonProperty("LogOptions")
    @JacksonXmlProperty(localName = "LogOptions", namespace = LRE_API_XMLNS)
    private LogOptions logOptions;

    @JsonProperty("ParametersSubstitution")
    @JacksonXmlProperty(localName = "ParametersSubstitution", namespace = LRE_API_XMLNS)
    private Boolean parametersSubstitution;

    @JsonProperty("DataReturnedByServer")
    @JacksonXmlProperty(localName = "DataReturnedByServer", namespace = LRE_API_XMLNS)
    private Boolean dataReturnedByServer;

    @JsonProperty("AdvanceTrace")
    @JacksonXmlProperty(localName = "AdvanceTrace", namespace = LRE_API_XMLNS)
    private Boolean advanceTrace;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogOptions {

        @JsonProperty("Type")
        @JacksonXmlProperty(isAttribute = true)
        private LogOptionsType type = LogOptionsType.ON_ERROR; // on error / always

        @JsonProperty("CacheSize")
        @JacksonXmlProperty(localName = "CacheSize", namespace = LRE_API_XMLNS)
        private Integer cacheSize = 1;
    }
}
