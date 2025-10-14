package com.lre.model.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LreRunStatus {

    @JsonProperty("ID")
    private int id;

    @JsonProperty("Duration")
    private int duration;

    @JsonProperty("RunState")
    private String runState;

    @JsonProperty("RunSLAStatus")
    private String runSlaStatus;

    @JsonProperty("TestID")
    private int testId;

    @JsonProperty("TestInstanceID")
    private int testInstanceId;

    @JsonProperty("PostRunAction")
    private String postRunAction;

    @JsonProperty("TimeslotID")
    private int timeslotId;

    @JsonProperty("VudsMode")
    private boolean vudsMode;

    @JsonProperty("StartTime")
    private String startTime;

    @JsonProperty("EndTime")
    private String endTime;

    @JsonProperty("MaxVusers")
    private int maxVusers;

    @JsonProperty("TotalPassedTransactions")
    private int totalPassedTransactions;

    @JsonProperty("TotalFailedTransactions")
    private int totalFailedTransactions;

    @JsonProperty("TotalErrors")
    private int totalErrors;

    @JsonProperty("AverageHitsPerSecond")
    private double averageHitsPerSecond;

    @JsonProperty("AverageThroughputPerSecond")
    private double averageThroughputPerSecond;

}