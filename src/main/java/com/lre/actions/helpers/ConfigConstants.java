package com.lre.actions.helpers;

import java.io.File;

public class ConfigConstants {


    // File System Constants
    public static final String DEFAULT_OUTPUT_DIR = new File(System.getProperty("user.dir")).getAbsolutePath();
    public static final String LRE_ARTIFACTS_DIR = "lre_artifacts";
    public static final String DEFAULT_TEST_FOLDER = "default_folder";

    // Logging Constants
    public static final String LRE_LOG_FILE = "lre_actions_%s.log";

    // API Constants
    public static final String LRE_API_XMLNS = "http://www.hp.com/PC/REST/API";
    public static final String LRE_API_BASE_URL = "%s/loadTest/rest";
    public static final String LRE_API_WEB_URL = "%s/loadTest/rest-pcweb";
    public static final String LRE_API_RESOURCES = "%s/domains/%s/projects/%s"; // Fixed
    public static final String DASHBOARD_URL = "%s/Loadtest/pcx-tab/run/%d/dashboard";

    // Authentication Endpoints
    public static final String LRE_AUTHENTICATE_WITH_USERNAME = "authentication-point/authenticate";
    public static final String LRE_AUTHENTICATE_WITH_TOKEN = "authentication-point/authenticateclient";
    public static final String LRE_LOGOUT = "authentication-point/logout";
    public static final String LRE_WEB_LOGIN_TO_PROJECT = "login/LoginToProject"; // Fixed typo

    // Resource Endpoints
    public static final String LRE_API_TESTS = "tests";
    public static final String TEST_INSTANCES_NAME = "testinstances";
    public static final String TEST_SETS_NAME = "testsets";
    public static final String TEST_SET_FOLDERS_NAME = "testfolders";
    public static final String DEFAULT_TEST_SET_NAME = "AutoTestSet";
    public static final String DEFAULT_TEST_SET_FOLDER_NAME = "AutoTestSet";
    public static final String TIMESLOT_CHECK = "designLoadTest/CalculateAvailability";
    public static final String START_RUN_API = "designLoadTest/StartRun";
    public static final String RUN_STATUS_API = "Runs";

    // Query Parameters
    public static final String TEST_INSTANCE_QUERY = "{test-id[\"%d\"]}";
    public static final String QUERY_PARAM = "query";

    // maximum duration allowed (480 hours = 20 days)
    public static final int MAX_HOURS = 480;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_DELAY_SECONDS = 5;
    public static final long DEFAULT_POLL_INTERVAL_SECONDS = 30;
    public static final long MILLIS_PER_MINUTE = 60_000L;


    public static final String LRE_RUN_ID_FILE = "lre_run_id.txt";
}
