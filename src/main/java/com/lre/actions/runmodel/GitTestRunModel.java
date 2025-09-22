package com.lre.actions.runmodel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the GitLab test run configuration.
 */
@Setter
@Getter
@Builder
@ToString(exclude = {"gitlabToken"})
public class GitTestRunModel {

    private boolean syncWithLre;
    private String gitServerUrl;
    private String branch;
    private String jobName;
    private String outputDir;
    private int projectId;
    private String gitlabToken;

}
