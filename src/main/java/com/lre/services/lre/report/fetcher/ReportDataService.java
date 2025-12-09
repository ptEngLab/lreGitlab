package com.lre.services.lre.report.fetcher;

import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreRunInfo;
import com.lre.model.report.LreTxnStats;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.ERRORS_DB_FORMAT;
import static com.lre.common.constants.ConfigConstants.RESULTS_DB_FORMAT;

public class ReportDataService {

    public static final int TOP_N = 10;

    /**
     * Resolves the database paths for results and errors, then fetches the corresponding report data.
     * <p>
     * This method resolves the database paths based on the provided `baseDbPath` and `runId`. It then fetches
     * the run information, transaction statistics, and error statistics by querying the respective databases.
     * </p>
     *
     * @param baseDbPath the base path of the database.
     * @param runId the ID of the run for which the report is being fetched.
     * @return a {@link ReportData} object containing the paths and fetched statistics.
     */
    public static ReportData fetchReportData(Path baseDbPath, int runId) {
        Path resultsDbPath = baseDbPath.resolve(String.format(RESULTS_DB_FORMAT, runId));
        Path errorsDbPath = baseDbPath.resolve(ERRORS_DB_FORMAT);

        LreRunInfo runInfo = ReportStatsFetcher.fetchRunInfo(resultsDbPath);
        List<LreTxnStats> txnStats = ReportStatsFetcher.fetchTransactions(resultsDbPath);
        List<LreErrorStats> errorStats = ReportStatsFetcher.fetchErrors(errorsDbPath);

        return new ReportData(resultsDbPath, errorsDbPath, runInfo, txnStats, errorStats);
    }

    /**
     * Returns at most the top N the slowest transactions based on the p95 percentile.
     * <p>
     * This method sorts the given list of transactions by the p95 value in descending order and returns the
     * top N transactions based on the slowest ones (highest p95).
     * </p>
     *
     * @param allStats the list of all transaction statistics.
     * @return a list containing the top N the slowest transactions.
     */
    public static List<LreTxnStats> getTopSlowestTransactions(List<LreTxnStats> allStats) {
        return allStats.stream()
                .sorted(Comparator.comparing(LreTxnStats::getP95).reversed())
                .limit(TOP_N)
                .toList();
    }

    /**
     * Returns at most the top N errors based on their occurrence.
     * <p>
     * This method limits the number of errors to the top N based on their order in the provided list.
     * </p>
     *
     * @param allStats the list of all error statistics.
     * @return a list containing the top N errors.
     */
    public static List<LreErrorStats> getTopErrors(List<LreErrorStats> allStats) {
        return allStats.stream()
                .limit(TOP_N)
                .toList();
    }

    /**
     * Immutable record holding both paths and fetched statistics for the report.
     * <p>
     * This record stores the paths to the result and error databases along with the statistics for the run,
     * transactions, and errors.
     * </p>
     */
    public record ReportData(
            Path resultsDbPath,   // Path to the results database
            Path errorsDbPath,    // Path to the errors database
            LreRunInfo runInfo,   // Run information (single row)
            List<LreTxnStats> transactions, // List of transaction statistics
            List<LreErrorStats> errors      // List of error statistics
    ) {
    }
}
