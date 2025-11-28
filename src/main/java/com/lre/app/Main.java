package com.lre.app;

import com.lre.client.runclient.EmailClient;
import com.lre.client.runclient.GitSyncClient;
import com.lre.client.runclient.LreRunClient;
import com.lre.client.runclient.ResultsExtractionClient;
import com.lre.client.runmodel.EmailConfigModel;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.LogHelper;
import com.lre.core.config.ReadConfigFile;
import com.lre.model.enums.Operation;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Main {

    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int EXIT_CONFIG_ERROR = 1;
    private static final int EXIT_IO_FAILURE = 2;
    private static final int EXIT_UNHANDLED_FAILURE = 3;
    private static final int EXIT_LRE_FAILURE = 4;

    public static void main(String[] args) {
        try {
            List<Operation> operations = parseArguments(args);
            if (operations.isEmpty()) {
                printHelp();
                System.exit(EXIT_CODE_SUCCESS);
            }

            // Setup logging
            String logLevelStr = System.getenv().getOrDefault("logLevel", "INFO");
            LogHelper.setup(logLevelStr, true);

            log.info("Starting operations: {}", operations);

            // Load configuration
            String configFilePath = getConfigFilePath(args);
            ReadConfigFile configFileData = new ReadConfigFile(configFilePath, operations.get(0));
            LreTestRunModel lreTestRunModel = configFileData.buildLreTestRunModel();
            GitTestRunModel gitTestRunModel = configFileData.buildGitTestRunModel();
            EmailConfigModel emailConfigModel = configFileData.buildEmailConfigModel();

            // Execute all operations in sequence
            boolean allOperationsSuccess = true;
            for (Operation operation : operations) {
                boolean operationResult = executeOperation(operation, lreTestRunModel, gitTestRunModel, emailConfigModel);
                if (!operationResult) {
                    allOperationsSuccess = false;
                    log.error("Operation {} failed.", operation);
                }
            }

            System.exit(allOperationsSuccess ? EXIT_CODE_SUCCESS : EXIT_LRE_FAILURE);

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

    private static List<Operation> parseArguments(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return List.of();  // No operations, show help
        }

        return Stream.of(args)
                .map(arg -> {
                    switch (arg.toLowerCase()) {
                        case "run" -> {
                            return Operation.RUN_LRE_TEST;
                        }
                        case "sync" -> {
                            return Operation.SYNC_GITLAB_WITH_LRE;
                        }
                        case "sendemail" -> {
                            return Operation.SEND_EMAIL;
                        }
                        case "extract" -> {
                            return Operation.EXTRACT_RESULTS;
                        }
                        default -> {
                            System.err.println("Unknown operation: " + arg);
                            return Operation.HELP;
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    private static boolean executeOperation(Operation operation, LreTestRunModel lreTestRunModel, GitTestRunModel gitTestRunModel, EmailConfigModel emailConfigModel) throws IOException {
        return switch (operation) {
            case RUN_LRE_TEST -> runLreTest(lreTestRunModel);
            case SYNC_GITLAB_WITH_LRE -> syncGitlabWithLre(gitTestRunModel, lreTestRunModel);
            case SEND_EMAIL -> sendEmail(emailConfigModel);
            case EXTRACT_RESULTS -> extractResults(lreTestRunModel);
            default -> {
                log.error("Unsupported operation: {}", operation);
                yield false;
            }
        };
    }

    private static boolean runLreTest(LreTestRunModel lreTestRunModel) throws LreException {
        try (LreRunClient lreRunClient = new LreRunClient(lreTestRunModel)) {
            lreRunClient.startRun();
            lreRunClient.printRunSummary();
            return true;
        }
    }

    private static boolean syncGitlabWithLre(GitTestRunModel gitTestRunModel, LreTestRunModel lreTestRunModel) throws LreException {
        log.info("Starting GitLab and LRE synchronization...");
        try (GitSyncClient gitSyncClient = new GitSyncClient(gitTestRunModel, lreTestRunModel)) {
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

    private static boolean extractResults(LreTestRunModel lreTestRunModel) {
        log.info("Starting the extraction of results...");

        try (ResultsExtractionClient extractClient = new ResultsExtractionClient(lreTestRunModel)) {
            extractClient.fetchRunDetails();
            extractClient.publishHtmlReportIfFinished();
            extractClient.publishAnalysedReportIfFinished();
            extractClient.extractRunReportsToExcel();
            extractClient.createRunResultsForEmail();
            return true;
        } catch (Exception e) {
            log.error("Error during results extraction for Run ID {}: {}", lreTestRunModel.getRunId(), e.getMessage(), e);
            return false;
        }

    }

    private static void printHelp() {
        System.out.println("Usage: java -jar lre-actions.jar <operation> [--config <path>]");
        System.out.println("Operations:");
        System.out.println("  run         Run LRE test");
        System.out.println("  sync        Sync GitLab with LRE");
        System.out.println("  sendemail   Send email with test results");
        System.out.println("  extract     Extract test results from LRE DB");
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
