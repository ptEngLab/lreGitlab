package com.lre.model.test.testcontent.groups.rts.pacing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lre.model.enums.PacingStartNewIterationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pacing {

    @JsonProperty("NumberOfIterations")
    @JacksonXmlProperty(localName = "NumberOfIterations", namespace = LRE_API_XMLNS)
    private int numberOfIterations = 1;

    @JsonProperty("StartNewIteration")
    @JacksonXmlProperty(localName = "StartNewIteration", namespace = LRE_API_XMLNS)
    private StartNewIteration startNewIteration = new StartNewIteration();


    // Inner class for StartNewIteration
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartNewIteration {

        @JsonProperty("Type")
        @JacksonXmlProperty(isAttribute = true, localName = "Type")
        private PacingStartNewIterationType type = PacingStartNewIterationType.IMMEDIATELY;

        @JsonProperty("DelayOfSeconds")
        @JacksonXmlProperty(localName = "DelayOfSeconds", namespace = LRE_API_XMLNS)
        private Integer delayOfSeconds;

        @JsonProperty("DelayAtRangeOfSeconds")
        @JacksonXmlProperty(localName = "DelayAtRangeOfSeconds", namespace = LRE_API_XMLNS)
        private Integer delayAtRangeOfSeconds;

        @JsonProperty("DelayAtRangeToSeconds")
        @JacksonXmlProperty(localName = "DelayAtRangeToSeconds", namespace = LRE_API_XMLNS)
        private Integer delayAtRangeToSeconds;

    }
}
