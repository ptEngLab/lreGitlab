package com.lre.model.yaml;

import lombok.Data;

@Data
public class YamlAutomaticTrending {
    private Integer reportId;
    private Integer maxRuns;
    private String trendRange;
    private String onMaxRuns;
    private Integer startTime;
    private Integer endTime;
}
