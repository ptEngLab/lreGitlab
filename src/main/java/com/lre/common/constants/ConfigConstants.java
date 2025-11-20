package com.lre.common.constants;

import java.io.File;
import java.util.regex.Pattern;

public class ConfigConstants {


    // File System Constants
    public static final String DEFAULT_OUTPUT_DIR = new File(System.getProperty("user.dir")).getAbsolutePath();
    public static final String ARTIFACTS_DIR = "artifacts";
    public static final String DEFAULT_TEST_FOLDER = "default_folder";

    public static final String HTML_REPORT_PATH = "%s/%s/LreReports/HtmlReport";  // Path format for HTML reports
    public static final String HTML_REPORTS_TYPE = "HTML REPORT";  // Type name for HTML reports
    public static final String HTML_REPORT_ARCHIVE_NAME = "Reports_%d.zip";

    public static final String ANALYSED_RESULTS_PATH = "%s/%s/LreReports/AnalysedReports";  // Path format for analyzed reports
    public static final String ANALYSED_RESULTS_TYPE = "ANALYZED RESULT";  // Type name for analyzed reports
    public static final String ANALYSED_REPORT_ARCHIVE_NAME = "Results_%d.zip";  // Analyzed report archive name (using runId)

    // Logging Constants "Type": "ANALYZED RESULT",
    public static final String LRE_LOG_FILE = "lre_actions_%s.log";

    // API Constants
    public static final String LRE_API_XMLNS = "http://www.hp.com/PC/REST/API";
    public static final String LRE_API_BASE_URL = "%s/loadTest/rest";
    public static final String LRE_API_WEB_URL = "%s/loadTest/rest-pcweb";
    public static final String LRE_API_RESOURCES = "%s/domains/%s/projects/%s"; // Fixed
    public static final String DASHBOARD_URL = "%s/Loadtest/pcx-tab/run/%d/dashboard";
    public static final String OPEN_DASHBOARD_URL = "loadtest/Services/DashboardService.asmx/OpenRunDashboard";
    public static final String TRANSACTIONS_DATA_URL = "loadtest/Services/OnlineGraphsService.asmx/GetRunTransactionsData";

    // Authentication Endpoints
    public static final String LRE_AUTHENTICATE_WITH_USERNAME = "authentication-point/authenticate";
    public static final String LRE_AUTHENTICATE_WITH_TOKEN = "authentication-point/authenticateclient";
    public static final String LRE_LOGOUT = "authentication-point/logout";
    public static final String LRE_WEB_LOGIN_TO_PROJECT = "login/LoginToProject"; // Fixed typo

    // Resource Endpoints
    public static final String LRE_API_TESTS = "tests";
    public static final String TEST_INSTANCES_NAME = "testinstances";
    public static final String TEST_SETS_NAME = "testsets";
    public static final String TEST_PLAN_NAME = "testplan";
    public static final String SCRIPTS_RESOURCE_NAME = "scripts";
    public static final String TEST_SET_FOLDERS_NAME = "testfolders";
    public static final String DEFAULT_TEST_SET_NAME = "AutoTestSet";
    public static final String DEFAULT_TEST_SET_FOLDER_NAME = "AutoTestSet";
    public static final String TIMESLOT_CHECK = "designLoadTest/CalculateAvailability";
    public static final String START_RUN_API = "designLoadTest/StartRun";
    public static final String ABORT_RUN_API = "abort";
    public static final String RUN_STATUS_API = "Runs";
    public static final String CLOUD_TEMPLATE_RESOURCE_NAME = "cloud/templates";
    public static final String HOST_RESOURCE_API = "Hosts";
    public static final String RESULTS_RESOURCE_API = "Results";

    // Query Parameters
    public static final String TEST_INSTANCE_QUERY = "{test-id[\"%d\"]}";
    public static final String QUERY_PARAM = "query";

    // maximum duration allowed (480 hours = 20 days)
    public static final int MAX_HOURS = 480;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_DELAY_SECONDS = 5;
    public static final long DEFAULT_POLL_INTERVAL_SECONDS = 0;


    public static final String LRE_RUN_ID_FILE = "lre_run_id.txt";

    public static final String basicByTest = "basic by test";
    public static final String basicByGroup = "basic by group";
    public static final String realWorldByTest = "real-world by test";
    public static final String realWorldByGroup = "real-world by group";


    // Simultaneously: "50vu:simultaneously+wait:30s" or "simultaneously" or "simultaneously:wait:1m"
    public static final Pattern SIMULTANEOUSLY_PATTERN =
            Pattern.compile("^(?:(?<vusersCount>\\d+)vu:)?simultaneously(?:[+:]?wait:(?<wait>(\\d+[dhms])+))?$",
                    Pattern.CASE_INSENSITIVE);

    // Gradually: "gradually:10u@30s" or "gradually:25u@1m+wait:10s" or "50vu:gradually:10u@30s"
    public static final Pattern GRADUALLY_PATTERN =
            Pattern.compile("^(?:(?<vusersCount>\\d+)vu:)?gradually:(?<users>\\d+)u@(?<interval>(\\d+[dhms])+)(?:[+:]wait:(?<wait>(\\d+[dhms])+))?$",
                    Pattern.CASE_INSENSITIVE);

    // Just before: "just before"
    public static final Pattern JUST_BEFORE_PATTERN =
            Pattern.compile("^just[ _-]?before$", Pattern.CASE_INSENSITIVE); // Allow "just-before", "just_before"

    // Run until complete: "until complete"
    public static final Pattern RUN_UNTIL_COMPLETE_PATTERN =
            Pattern.compile("^until[ _-]?complete$", Pattern.CASE_INSENSITIVE); // Allow "until-complete"

    // Run for duration: "1h30m" or "30s" or "2d"
    public static final Pattern RUN_FOR_PATTERN =
            Pattern.compile("^(?<interval>(\\d+[dhms])+)$", Pattern.CASE_INSENSITIVE);

    // Start group delay: "delay:30s" or "delay:1h"
    public static final Pattern START_GROUP_DELAY_PATTERN =
            Pattern.compile("^delay:(?<interval>(\\d+[dhms])+)$", Pattern.CASE_INSENSITIVE);

    // Start group finish: "Group1" or "API_Users"
    public static final Pattern START_GROUP_FINISH_PATTERN =
            Pattern.compile("^after (?<groupName>[a-zA-Z0-9_-]+)$", Pattern.CASE_INSENSITIVE);


    public static final String GIT_REPO_EXTRACT_PATH = "GitLabRepo";
    public static final String GIT_COMMIT_HISTORY_FILE = "Gitlab_Commit_History.json";

    public static final String COMMIT_HISTORY_ARTIFACT_PATH = ARTIFACTS_DIR + "/" + GIT_REPO_EXTRACT_PATH + "/" + GIT_COMMIT_HISTORY_FILE;


    public static final int QUERY_TIMEOUT_SECONDS = 3600; // 1 hour for large queries
    public static final int QUERY_BATCH_SIZE = 10000;


}
