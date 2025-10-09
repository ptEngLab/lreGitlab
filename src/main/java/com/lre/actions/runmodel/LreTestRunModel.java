package com.lre.actions.runmodel;


import com.lre.model.enums.PostRunAction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString(exclude = {"password"})
public class LreTestRunModel {

    private String lreServerUrl;
    private String userName;
    private String password;
    private String domain;
    private String project;
    private boolean authenticateWithToken;
    private int testId;
    private String testName;
    private String testFolderPath;
    private String testToRun;
    private boolean existingTest;
    private String testContentToCreate;
    private int testInstanceId;
    private int timeslotDurationHours;
    private int timeslotDurationMinutes;

    private PostRunAction lrePostRunAction;
    private boolean virtualUserFlexDaysMode;
    private int virtualUserFlexDaysAmount;
    private String description;
    private boolean searchTimeslot;
    private String statusBySla;
    private String workspace;
    private boolean runTestFromGitlab;

    private int runId;
    private int lreInternalRunId;
    private String dashboardUrl;
    private int timeslotId;
    private boolean htmlReportAvailable;


}
