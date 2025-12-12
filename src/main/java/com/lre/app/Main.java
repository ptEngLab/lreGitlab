package com.lre.app;

import com.lre.client.runmodel.EmailConfigModel;
import com.lre.client.runmodel.GitTestRunModel;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.exceptions.LreException;
import com.lre.common.exceptions.OperationExecutionException;
import com.lre.common.utils.LogHelper;
import com.lre.core.config.ReadConfigFile;
import com.lre.model.enums.Operation;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class Main {

    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int EXIT_CONFIG_ERROR = 1;
    private static final int EXIT_IO_FAILURE = 2;
    private static final int EXIT_UNHANDLED_FAILURE = 3;
    private static final int EXIT_LRE_FAILURE = 4;

    @FunctionalInterface
    private interface OperationExecutor {
        boolean execute(ExecutionContext ctx) throws OperationExecutionException;
    }

    private record ExecutionContext(
            LreTestRunModel lre,
            GitTestRunModel git,
            EmailConfigModel email
    ) {}

    private static final OperationService OPERATION_SERVICE = new OperationService();

    private static final Map<Operation, OperationExecutor> EXECUTORS = Map.of(
            Operation.RUN_LRE_TEST, ctx -> OPERATION_SERVICE.runLreTest(ctx.lre()),
            Operation.SYNC_GITLAB_WITH_LRE, ctx -> OPERATION_SERVICE.syncGitlabWithLre(ctx.git(), ctx.lre()),
            Operation.SEND_EMAIL, ctx -> OPERATION_SERVICE.sendEmail(ctx.email(), ctx.lre()),
            Operation.EXTRACT_RESULTS, ctx -> OPERATION_SERVICE.extractResults(ctx.lre())
    );

    public static void main(String[] args) {
        int exitCode;

        try {
            List<Operation> operations = parseArguments(args);
            if (operations.isEmpty()) {
                printHelp();
                exitCode = EXIT_CODE_SUCCESS;
                System.exit(exitCode);
            }

            setupLogging();

            String configFilePath = getConfigFilePath(args);
            ReadConfigFile config = new ReadConfigFile(configFilePath, operations.get(0));
            ExecutionContext ctx = new ExecutionContext(
                    config.buildLreTestRunModel(),
                    config.buildGitTestRunModel(),
                    config.buildEmailConfigModel()
            );

            exitCode = executeOperations(operations, ctx);

        } catch (LreException e) {
            log.error("LRE execution error: {}", e.getMessage(), e);
            exitCode = EXIT_LRE_FAILURE;

        } catch (IllegalArgumentException e) {
            log.error("Configuration error: {}", e.getMessage(), e);
            exitCode = EXIT_CONFIG_ERROR;

        } catch (IOException e) {
            log.error("I/O error: {}", e.getMessage(), e);
            exitCode = EXIT_IO_FAILURE;

        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            exitCode = EXIT_UNHANDLED_FAILURE;
        }

        System.exit(exitCode);
    }

    private static void setupLogging() {
        String logLevel = System.getenv().getOrDefault("logLevel", "INFO");
        LogHelper.setup(logLevel, true);
    }

    private static int executeOperations(List<Operation> operations, ExecutionContext ctx) {
        log.info("Executing operations: {}", operations);
        boolean success = true;

        for (Operation op : operations) {
            OperationExecutor executor = EXECUTORS.get(op);
            if (executor == null) {
                log.error("Unsupported operation: {}", op);
                success = false;
                continue;
            }

            try {
                if (!executor.execute(ctx)) {
                    log.error("Operation {} failed", op);
                    success = false;
                }
            } catch (OperationExecutionException e) {
                log.error("Operation {} failed: {}", op, e.getMessage(), e);
                success = false;
            }
        }

        return success ? EXIT_CODE_SUCCESS : EXIT_LRE_FAILURE;
    }

    private static List<Operation> parseArguments(String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            return List.of();
        }

        return Stream.of(args)
                .map(arg -> switch (arg.toLowerCase()) {
                    case "run" -> Operation.RUN_LRE_TEST;
                    case "sync" -> Operation.SYNC_GITLAB_WITH_LRE;
                    case "sendemail" -> Operation.SEND_EMAIL;
                    case "extract" -> Operation.EXTRACT_RESULTS;
                    default -> {
                        log.warn("Unknown operation: {}", arg);
                        yield Operation.HELP;
                    }
                })
                .filter(op -> op != Operation.HELP)
                .toList();
    }

    private static void printHelp() {
        log.info("""
                Usage: java -jar lre-actions.jar <operation> [--config <path>]

                Operations:
                  run         Run LRE test
                  sync        Sync GitLab with LRE
                  sendemail   Send email with test results
                  extract     Extract test results
                  help        Show this help message

                Options:
                  -c, --config <path>   Use specific config file (default ./config.json)

                Env:
                  logLevel=DEBUG|INFO|WARN|ERROR  (default INFO)
                """);
    }

    private static String getConfigFilePath(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i]) || "-c".equals(args[i])) {
                return args[i + 1];
            }
        }
        return System.getProperty("user.dir") + File.separator + "config.json";
    }
}
