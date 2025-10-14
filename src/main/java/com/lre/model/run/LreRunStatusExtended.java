package com.lre.model.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
public class LreRunStatusExtended {

    @JsonProperty("Id")
    private int id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("State")
    private String state;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Duration")
    private int duration;

    @JsonProperty("Tester")
    private String tester;

    @JsonProperty("TestName")
    private String testName;

    @JsonProperty("TestId")
    private int testId;

    @JsonProperty("TestInstanceId")
    private int testInstanceId;

    @JsonProperty("TestSetName")
    private String testSetName;

    @JsonProperty("TestSetId")
    private int testSetId;

    @JsonProperty("ReservationId")
    private int reservationId;

    @JsonProperty("Controller")
    private String controller;

    @JsonProperty("LGs")
    private String lgs;

    @JsonProperty("VusersInvolved")
    private int vusersInvolved;

    @JsonProperty("VusersMax")
    private int vusersMax;

    @JsonProperty("VusersAvg")
    private int vusersAvg;

    @JsonProperty("Errors")
    private int errors;

    @JsonProperty("TransPassed")
    private int transPassed;

    @JsonProperty("TransFailed")
    private int transFailed;

    @JsonProperty("TransPerSec")
    private int transPerSec;

    @JsonProperty("HitsPerSec")
    private int hitsPerSec;

    @JsonProperty("ThroughputAvg")
    private int throughputAvg;

    @JsonProperty("Start")
    private LocalDateTime start;

    @JsonProperty("End")
    private LocalDateTime end;

    @JsonProperty("Modified")
    private Date modified;

    @JsonProperty("UsingVuds")
    private boolean usingVuds;

    @JsonProperty("VudsAmount")
    private int vudsAmount;

    @JsonProperty("AnalysisTemplateId")
    private int analysisTemplateId;

    @JsonProperty("Hosts")
    private String hosts;

    @JsonProperty("Comments")
    private String comments;

    @JsonProperty("JenkinsJobName")
    private String jenkinsJobName;

    @JsonProperty("OperatingSystem")
    private String operatingSystem;

    @JsonProperty("TestDescription")
    private String testDescription;

    @JsonProperty("SLADefined")
    private int slaDefined;

    @JsonProperty("Progress")
    private String progress;

    @JsonProperty("UsedDynamicTypes")
    private String usedDynamicTypes;

}
