package com.lre.actions.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ParameterDefinitions {

    /**
     * Default values for all configuration parameters.
     */
    public static final class Defaults {
        // LRE Parameters
        public static final String LRE_USERNAME = "";
        public static final String LRE_PASSWORD = "";
        public static final String LRE_DOMAIN = "";
        public static final String LRE_PROJECT = "";
        public static final String LRE_TEST = "";
        public static final String LRE_SERVER = "https://lre.example.com";
        public static final String LRE_POST_RUN_ACTION = "Collate and Analyse";
        public static final String LRE_DESCRIPTION = "Automated test run";
        public static final boolean LRE_SEARCH_TIMESLOT = false;
        public static final String LRE_STATUS_BY_SLA = "";
        public static final String LRE_OUTPUT_DIR = System.getProperty("user.dir");
        public static final boolean RUN_LRE_TEST_FROM_GITLAB_FLAG = false;
        public static final int LRE_TIMESLOT_DURATION_HOURS = 0;
        public static final int LRE_TIMESLOT_DURATION_MINUTES = 30;
        public static final int AUTO_TEST_INSTANCE_ID = -1;
        public static final boolean VIRTUAL_USER_FLEX_DAYS_MODE = false;
        public static final int VIRTUAL_USER_FLEX_DAYS_AMOUNT = 0;


        // GitLab Parameters
        public static final boolean SYNC_GITLAB_WITH_LRE_FLAG = true;
        public static final String GITLAB_SERVER = "https://gitlab.example.com";
        public static final String GITLAB_BRANCH = "main";
        public static final String GITLAB_JOB_NAME = "lre-test-job";
        public static final String GITLAB_OUTPUT_DIR = System.getProperty("user.dir");
        public static final int GITLAB_PROJECT_ID = 0;
        public static final String GITLAB_TOKEN = "";
    }

    /**
     * Keys used for JSON/env variables.
     */
    public static final class Keys {
        // LRE Parameters
        public static final String LRE_USERNAME = "lre_username";
        public static final String LRE_PASSWORD = "lre_password";
        public static final String LRE_DOMAIN = "lre_domain";
        public static final String LRE_PROJECT = "lre_project";
        public static final String LRE_TEST = "lre_test";
        public static final String LRE_SERVER = "lre_server";
        public static final String LRE_TIMESLOT_DURATION_HOURS = "lre_timeslot_duration_hours";
        public static final String LRE_TIMESLOT_DURATION_MINUTES = "lre_timeslot_duration_minutes";
        public static final String LRE_SEARCH_TIMESLOT = "lre_search_timeslot";
        public static final String LRE_STATUS_BY_SLA = "lre_status_by_sla";
        public static final String LRE_TEST_INSTANCE = "lre_test_instance";
        public static final String RUN_LRE_TEST_FROM_GITLAB_FLAG = "run_lre_test_from_gitlab_flag";
        public static final String LRE_POST_RUN_ACTION = "lre_post_run_action";
        public static final String LRE_DESCRIPTION = "lre_description";
        public static final String LRE_OUTPUT_DIR = "lre_output_dir";
        public static final String VIRTUAL_USER_FLEX_DAYS_MODE = "virtual_user_flex_days_mode";
        public static final String VIRTUAL_USER_FLEX_DAYS_AMOUNT = "virtual_user_flex_days_amount";

        // GitLab Parameters
        public static final String SYNC_GITLAB_WITH_LRE_FLAG = "sync_gitlab_with_lre_flag";
        public static final String GITLAB_SERVER = "gitlab_server";
        public static final String GITLAB_BRANCH = "gitlab_branch";
        public static final String GITLAB_JOB_NAME = "gitlab_job_name";
        public static final String GITLAB_TOKEN = "gitlab_token";
        public static final String GITLAB_OUTPUT_DIR = "gitlab_output_dir";
        public static final String GITLAB_PROJECT_ID = "gitlab_project_id";
    }

    /**
     * Represents a configuration parameter with metadata.
     * @param <T> The type of the parameter value.
     */
    public static class ConfigParameter<T> {
        public final String key;
        public final boolean required;
        public final T defaultValue;
        public final boolean conditional;

        public ConfigParameter(String key, boolean required, T defaultValue) {
            this(key, required, defaultValue, false);
        }

        public ConfigParameter(String key, boolean required, T defaultValue, boolean conditional) {
            this.key = key;
            this.required = required;
            this.defaultValue = defaultValue;
            this.conditional = conditional;
        }
    }

    /**
     * Returns all parameter definitions used in the system.
     */
    public static List<ConfigParameter<?>> getDefinitions() {
        // LRE Parameters
        List<ConfigParameter<?>> lreParams = Arrays.asList(
                new ConfigParameter<>(Keys.LRE_USERNAME, true, Defaults.LRE_USERNAME),
                new ConfigParameter<>(Keys.LRE_PASSWORD, true, Defaults.LRE_PASSWORD),
                new ConfigParameter<>(Keys.LRE_DOMAIN, true, Defaults.LRE_DOMAIN),
                new ConfigParameter<>(Keys.LRE_PROJECT, true, Defaults.LRE_PROJECT),
                new ConfigParameter<>(Keys.LRE_TEST, true, Defaults.LRE_TEST),
                new ConfigParameter<>(Keys.LRE_TIMESLOT_DURATION_HOURS, false, Defaults.LRE_TIMESLOT_DURATION_HOURS),
                new ConfigParameter<>(Keys.LRE_TIMESLOT_DURATION_MINUTES, false, Defaults.LRE_TIMESLOT_DURATION_MINUTES),
                new ConfigParameter<>(Keys.LRE_SERVER, false, Defaults.LRE_SERVER),
                new ConfigParameter<>(Keys.LRE_POST_RUN_ACTION, false, Defaults.LRE_POST_RUN_ACTION),
                new ConfigParameter<>(Keys.LRE_DESCRIPTION, false, Defaults.LRE_DESCRIPTION),
                new ConfigParameter<>(Keys.LRE_SEARCH_TIMESLOT, false, Defaults.LRE_SEARCH_TIMESLOT),
                new ConfigParameter<>(Keys.LRE_STATUS_BY_SLA, false, Defaults.LRE_STATUS_BY_SLA),
                new ConfigParameter<>(Keys.LRE_TEST_INSTANCE, false, Defaults.AUTO_TEST_INSTANCE_ID),
                new ConfigParameter<>(Keys.LRE_OUTPUT_DIR, false, Defaults.LRE_OUTPUT_DIR),
                new ConfigParameter<>(Keys.RUN_LRE_TEST_FROM_GITLAB_FLAG, false, Defaults.RUN_LRE_TEST_FROM_GITLAB_FLAG),
                new ConfigParameter<>(Keys.VIRTUAL_USER_FLEX_DAYS_MODE, false, Defaults.VIRTUAL_USER_FLEX_DAYS_MODE),
                new ConfigParameter<>(Keys.VIRTUAL_USER_FLEX_DAYS_AMOUNT, false, Defaults.VIRTUAL_USER_FLEX_DAYS_AMOUNT)
        );

        // GitLab Parameters
        List<ConfigParameter<?>> gitlabParams = Arrays.asList(
                new ConfigParameter<>(Keys.SYNC_GITLAB_WITH_LRE_FLAG, false, Defaults.SYNC_GITLAB_WITH_LRE_FLAG),
                new ConfigParameter<>(Keys.GITLAB_TOKEN, false, Defaults.GITLAB_TOKEN, true),
                new ConfigParameter<>(Keys.GITLAB_PROJECT_ID, false, Defaults.GITLAB_PROJECT_ID, true),
                new ConfigParameter<>(Keys.GITLAB_SERVER, false, Defaults.GITLAB_SERVER),
                new ConfigParameter<>(Keys.GITLAB_BRANCH, false, Defaults.GITLAB_BRANCH),
                new ConfigParameter<>(Keys.GITLAB_JOB_NAME, false, Defaults.GITLAB_JOB_NAME),
                new ConfigParameter<>(Keys.GITLAB_OUTPUT_DIR, false, Defaults.GITLAB_OUTPUT_DIR)
        );

        // Combine LRE + GitLab
        return Stream.concat(lreParams.stream(), gitlabParams.stream()).toList();
    }
}
