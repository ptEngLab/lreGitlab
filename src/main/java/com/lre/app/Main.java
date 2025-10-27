package com.lre.app;

import com.lre.client.runclient.EmailClient;
import com.lre.client.runclient.GitSyncClient;
import com.lre.client.runclient.LreRunClient;
import com.lre.client.runmodel.EmailConfigModel;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.LogHelper;
import com.lre.core.config.ReadConfigFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import static com.lre.common.utils.CommonUtils.removeRunIdFile;

@Slf4j
public class Main {

    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int EXIT_CONFIG_ERROR = 1;
    private static final int EXIT_IO_FAILURE = 2;
    private static final int EXIT_UNHANDLED_FAILURE = 3;
    private static final int EXIT_LRE_FAILURE = 4;

    private enum Operation {
        RUN_LRE_TEST,
        SYNC_GITLAB_WITH_LRE,
        SEND_EMAIL,
        HELP
    }

    public static void main(String[] args) {
        try {
            Operation operation = parseArguments(args);
            if (operation == Operation.HELP) {
                printHelp();
                System.exit(EXIT_CODE_SUCCESS);
            }

            // Setup logging
            String logLevelStr = System.getenv().getOrDefault("logLevel", "INFO");
            LogHelper.setup(logLevelStr, true);
            log.info("Starting operation: {}", operation);

            // Load configuration
            String configFilePath = getConfigFilePath(args);
            ReadConfigFile configFileData = new ReadConfigFile(configFilePath);
            LreTestRunModel lreTestRunModel = configFileData.buildLreTestRunModel();
            GitTestRunModel gitTestRunModel = configFileData.buildGitTestRunModel();
            EmailConfigModel emailConfigModel = configFileData.buildEmailConfigModel();


            // Run requested operation
            boolean operationResult = switch (operation) {
                case RUN_LRE_TEST -> runLreTest(lreTestRunModel);
                case SYNC_GITLAB_WITH_LRE -> syncGitlabWithLre(gitTestRunModel, lreTestRunModel);
                case SEND_EMAIL -> sendEmail(emailConfigModel);
                default -> false;
            };

            System.exit(operationResult ? EXIT_CODE_SUCCESS : EXIT_LRE_FAILURE);

        } catch (LreException e) {
            exitWithError(EXIT_LRE_FAILURE, "LRE execution error", e);
        } catch (IllegalArgumentException e) {
            exitWithError(EXIT_CONFIG_ERROR, "Configuration error", e);
        } catch (IOException e) {
            exitWithError(EXIT_IO_FAILURE, "I/O error", e);
        } catch (Exception e) {
            exitWithError(EXIT_UNHANDLED_FAILURE, "Unhandled error", e);
        }
    }

    private static Operation parseArguments(String[] args) {
        if (args.length == 0) {
            return Operation.HELP;
        }

        String operationArg = args[0].toLowerCase();
        return switch (operationArg) {
            case "run" -> Operation.RUN_LRE_TEST;
            case "sync" -> Operation.SYNC_GITLAB_WITH_LRE;
            case "sendemail" -> Operation.SEND_EMAIL;
            case "help", "--help", "-h" -> Operation.HELP;
            default -> {
                System.err.println("Unknown operation: " + args[0]);
                yield Operation.HELP;
            }
        };
    }

    private static boolean runLreTest(LreTestRunModel lreTestRunModel) throws LreException, IOException {
        try (LreRunClient lreRunClient = new LreRunClient(lreTestRunModel)) {
            lreRunClient.startRun();
            lreRunClient.publishRunReport();
            lreRunClient.printRunSummary();
            removeRunIdFile();
            return true;
        }
    }

    private static boolean syncGitlabWithLre(GitTestRunModel gitTestRunModel, LreTestRunModel lreTestRunModel) throws LreException, IOException {
        log.info("Starting GitLab and LRE synchronization...");

        try(GitSyncClient gitSyncClient = new GitSyncClient(gitTestRunModel, lreTestRunModel)) {
            return gitSyncClient.sync();
        }
    }

    private static boolean sendEmail(EmailConfigModel emailConfigModel) {
        log.info("Starting email sending process...");

        try (EmailClient emailClient = new EmailClient(emailConfigModel)) {
            return emailClient.send();
        } catch (Exception e) {
            log.error("Error during email send operation: {}", e.getMessage(), e);
            return false;
        }
    }


    private static void printHelp() {
        System.out.println("Usage: java -jar lre-actions.jar <operation> [--config <path>]");
        System.out.println("Operations:");
        System.out.println("  run         Run LRE test");
        System.out.println("  sync        Sync GitLab with LRE");
        System.out.println("  sendemail   Send email with test results");
        System.out.println("  help        Show this help message");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -c, --config <path>   Specify configuration file (default: ./config.json)");
        System.out.println();
        System.out.println("Environment:");
        System.out.println("  logLevel=DEBUG|INFO|WARN|ERROR   Set logging level (default: INFO)");
    }

    private static String getConfigFilePath(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i]) || "-c".equals(args[i])) {
                return args[i + 1];
            }
        }
        return System.getProperty("user.dir") + File.separator + "config.json";
    }



    private static void exitWithError(int code, String message, Exception e) {
        log.error("{}: {}", message, e.getMessage(), e);
        System.err.printf("Exiting with code %d: %s%n", code, message);
        System.exit(code);
    }
}
