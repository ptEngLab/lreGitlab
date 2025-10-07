package com.lre.model.test.testcontent.sla;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SLAConfig {
    // For Average Response Time SLA
    @JsonProperty("AvgResponseTimeLoadCriteria")
    private String avgResponseTimeLoadCriteria;

    @JsonProperty("AvgResponseTimeLoadRanges")
    private List<Integer> avgResponseTimeLoadRanges;

    @JsonProperty("AvgResponseTimeThresholds")
    private Map<String, List<Integer>> avgResponseTimeThresholds;

    // For Errors Per Second SLA
    @JsonProperty("ErrorLoadCriteriaType")
    private String errorLoadCriteriaType;

    @JsonProperty("ErrorLoadRanges")
    private List<Integer> errorLoadRanges;

    @JsonProperty("ErrorThreshold")
    private List<Integer> errorThreshold;

    // For Percentile Response Time SLA
    @JsonProperty("PercentileResponseTimeThreshold")
    private Integer percentileResponseTimeThreshold;

    @JsonProperty("PercentileResponseTimeTransactions")
    private Map<String, Integer> percentileResponseTimeTransactions;

    @JsonProperty("TotalHits")
    private Integer totalHits;

    @JsonProperty("AverageHitsPerSecond")
    private Integer avgHitsPerSecond;

    @JsonProperty("TotalThroughput")
    private Integer totalThroughput;

    @JsonProperty("AverageThroughput")
    private Integer avgThroughput;
}