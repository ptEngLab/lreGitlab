package com.lre.model.test.testcontent.sla;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SLAConfig {
    // For Average Response Time SLA
    private String avgResponseTimeLoadCriteria;
    private List<Integer> avgResponseTimeLoadRanges;
    private Map<String, List<Integer>> avgResponseTimeThresholds;

    // For Errors Per Second SLA
    private String errorLoadCriteriaType;
    private List<Integer> errorLoadRanges;
    private List<Integer> errorThreshold;

    // For Percentile Response Time SLA
    private Integer percentileResponseTimeThreshold;
    private Map<String, Integer> percentileResponseTimeTransactions;

    // For Simple SLA types
    private Integer totalHits;
    private Integer avgHitsPerSecond;
    private Integer totalThroughput;
    private Integer avgThroughput;
}