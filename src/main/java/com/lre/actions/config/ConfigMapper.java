package com.lre.actions.config;

import com.lre.actions.helpers.TestFileHelper;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.runmodel.GitTestRunModel;
import com.lre.actions.common.entities.base.run.PostRunAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class ConfigMapper {

    public LreTestRunModel mapToLreModel(Map<String, Object> params) throws IOException {
        LreTestRunModel.LreTestRunModelBuilder builder = LreTestRunModel.builder()
                .lreServerUrl((String) params.get(ParameterDefinitions.Keys.LRE_SERVER))
                .userName((String) params.get(ParameterDefinitions.Keys.LRE_USERNAME))
                .password((String) params.get(ParameterDefinitions.Keys.LRE_PASSWORD))
                .domain((String) params.get(ParameterDefinitions.Keys.LRE_DOMAIN))
                .project((String) params.get(ParameterDefinitions.Keys.LRE_PROJECT))
                .testToRun((String) params.get(ParameterDefinitions.Keys.LRE_TEST))
                .testInstanceId((int) params.get(ParameterDefinitions.Keys.LRE_TEST_INSTANCE))
                .timeslotDurationHours((Integer) params.get(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_HOURS))
                .timeslotDurationMinutes((Integer) params.get(ParameterDefinitions.Keys.LRE_TIMESLOT_DURATION_MINUTES))
                .lrePostRunAction(parsePostRunAction(params))
                .virtualUserFlexDaysMode((Boolean) params.get(ParameterDefinitions.Keys.VIRTUAL_USER_FLEX_DAYS_MODE))
                .virtualUserFlexDaysAmount((int) params.get(ParameterDefinitions.Keys.VIRTUAL_USER_FLEX_DAYS_AMOUNT))
                .description((String) params.get(ParameterDefinitions.Keys.LRE_DESCRIPTION))
                .authenticateWithToken(getAuthType(params))
                .searchTimeslot((Boolean) params.get(ParameterDefinitions.Keys.LRE_SEARCH_TIMESLOT))
                .statusBySla((String) params.get(ParameterDefinitions.Keys.LRE_STATUS_BY_SLA))
                .workspace((String) params.get(ParameterDefinitions.Keys.LRE_OUTPUT_DIR))
                .runTestFromGitlab((Boolean) params.get(ParameterDefinitions.Keys.RUN_LRE_TEST_FROM_GITLAB_FLAG))
                .existingTest(false)
                .testContentToCreate(null)
                .testFolderPath(null)
                .testId(0)
                .runId(0)
                .lreInternalRunId(0)
                .dashboardUrl(null)
                .timeslotId(0);

        resolveTestDetails(params, builder);
        return builder.build();
    }

    public GitTestRunModel mapToGitModel(Map<String, Object> params) {
        return GitTestRunModel.builder()
                .syncWithLre((Boolean) params.get(ParameterDefinitions.Keys.SYNC_GITLAB_WITH_LRE_FLAG))
                .gitServerUrl((String) params.get(ParameterDefinitions.Keys.GITLAB_SERVER))
                .branch((String) params.get(ParameterDefinitions.Keys.GITLAB_BRANCH))
                .jobName((String) params.get(ParameterDefinitions.Keys.GITLAB_JOB_NAME))
                .outputDir((String) params.get(ParameterDefinitions.Keys.GITLAB_OUTPUT_DIR))
                .projectId((Integer) params.get(ParameterDefinitions.Keys.GITLAB_PROJECT_ID))
                .gitlabToken((String) params.get(ParameterDefinitions.Keys.GITLAB_TOKEN))
                .build();
    }

    private boolean getAuthType(Map<String, Object> params) {
        String username = (String) params.get(ParameterDefinitions.Keys.LRE_USERNAME);
        String password = (String) params.get(ParameterDefinitions.Keys.LRE_PASSWORD);
        return username.startsWith("I_KEY_") && password.startsWith("S_KEY_");
    }

    private PostRunAction parsePostRunAction(Map<String, Object> params) {
        String value = params.get(ParameterDefinitions.Keys.LRE_POST_RUN_ACTION).toString().trim();
        try {
            return PostRunAction.fromString(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid PostRunAction '{}', defaulting to COLLATE_AND_ANALYSE", value);
            return PostRunAction.COLLATE_AND_ANALYSE;
        }
    }

    private void resolveTestDetails(Map<String, Object> params, LreTestRunModel.LreTestRunModelBuilder builder) {
        String testValue = (String) params.get(ParameterDefinitions.Keys.LRE_TEST);
        Path workspacePath = Paths.get((String) params.get(ParameterDefinitions.Keys.LRE_OUTPUT_DIR)).toAbsolutePath().normalize();

        // YAML test file
        if (TestFileHelper.isYamlTest(testValue)) {
            TestFileHelper.TestFileDetails details = TestFileHelper.getTestFileDetails(workspacePath, testValue);
            builder.testName(details.name())
                    .testContentToCreate(details.content())
                    .testFolderPath(details.folder())
                    .existingTest(false);
            return;
        }

        // Numeric ID
        if (StringUtils.isNumeric(testValue)) {
            builder.testId(Integer.parseInt(testValue))
                    .existingTest(true)
                    .testContentToCreate(null);
            return;
        }

        // Plain test name
        builder.testName(testValue)
                .existingTest(true)
                .testContentToCreate(null);
    }
}
