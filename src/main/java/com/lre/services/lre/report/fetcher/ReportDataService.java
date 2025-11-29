package com.lre.services.lre.report.fetcher;

import com.lre.model.report.LreErrorStats;
import com.lre.model.report.LreTxnStats;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static com.lre.common.constants.ConfigConstants.ERRORS_DB_FORMAT;
import static com.lre.common.constants.ConfigConstants.RESULTS_DB_FORMAT;

public class ReportDataService {

    public static final int TOP_N = 10;

    /**
     * Resolves DB paths and fetches both transactions and errors.
     */
    public static ReportData fetchReportData(Path baseDbPath, int runId) {
        Path resultsDbPath = baseDbPath.resolve(String.format(RESULTS_DB_FORMAT, runId));
        Path errorsDbPath = baseDbPath.resolve(ERRORS_DB_FORMAT);

        List<LreTxnStats> txnStats = ReportStatsFetcher.fetchTransactions(resultsDbPath);
        List<LreErrorStats> errorStats = ReportStatsFetcher.fetchErrors(errorsDbPath);

        return new ReportData(resultsDbPath, errorsDbPath, txnStats, errorStats);
    }

    /**
     * Returns at most the top N the slowest transactions based on p95.
     */
    public static List<LreTxnStats> getTopSlowestTransactions(List<LreTxnStats> allStats) {
        return allStats.stream()
                .sorted(Comparator.comparing(LreTxnStats::getP95).reversed())
                .limit(TOP_N)
                .toList();
    }

    /**
     * Returns at most the top N errors.
     */
    public static List<LreErrorStats> getTopErrors(List<LreErrorStats> allStats) {
        return allStats.stream()
                .limit(TOP_N)
                .toList();
    }

    /**
     * Immutable record holding both paths and stats.
     */
    public record ReportData(
            Path resultsDbPath,
            Path errorsDbPath,
            List<LreTxnStats> transactions,
            List<LreErrorStats> errors
    ) {
    }
}
