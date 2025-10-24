package com.lre.model.test.testcontent.automatictrending;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.common.constants.ConfigConstants.LRE_API_XMLNS;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutomaticTrending {

    @JsonProperty("ReportId")
    @JacksonXmlProperty(localName = "ReportId", namespace = LRE_API_XMLNS)
    private Integer reportId;

    @JsonProperty("MaxRunsInReport")
    @JacksonXmlProperty(localName = "MaxRunsInReport", namespace = LRE_API_XMLNS)
    private Integer maxRuns = 20;

    @JsonProperty("TrendRangeType")
    @JacksonXmlProperty(localName = "TrendRangeType", namespace = LRE_API_XMLNS)
    private TrendRangeType trendRange = TrendRangeType.CompleteRun;

    @JsonProperty("MaxRunsReachedOption")
    @JacksonXmlProperty(localName = "MaxRunsReachedOption", namespace = LRE_API_XMLNS)
    private MaxRunsReachedOption onMaxRuns = MaxRunsReachedOption.DeleteFirstSetNewBaseline;

    @JsonProperty("StartTime")
    @JacksonXmlProperty(localName = "StartTime", namespace = LRE_API_XMLNS)
    private Integer startTime = 20; // in minutes, required if trendRange = PartOfRun

    @JsonProperty("EndTime")
    @JacksonXmlProperty(localName = "EndTime", namespace = LRE_API_XMLNS)
    private Integer endTime = 80;   // in minutes, required if trendRange = PartOfRun


    public enum TrendRangeType {
        CompleteRun, PartOfRun
    }

    public enum MaxRunsReachedOption {
        DoNotPublishAdditionalRuns,
        DeleteFirstSetNewBaseline
    }
}
