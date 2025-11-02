package com.lre.services.lre.summary;

import com.lre.client.api.lre.LreRestApis;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.utils.JsonUtils;
import com.lre.model.run.LreOpenRunDashboardResponse;
import com.lre.model.transactions.LreTransactionMetrics;
import com.lre.model.transactions.LreTransactionMetricsRequest;
import com.lre.model.transactions.LreTransactionMetricsResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public record LreTxnSummaryFetcher(LreRestApis lreRestApis, LreTestRunModel model) {

    private static final int PAGE_SIZE = 50;

    /**
     * Fetches all transaction metrics for the given run, handling pagination and returns the list of transactions.
     */

    public List<LreTransactionMetrics> fetchTransactionSummary() {
        try {
            log.debug("Fetching transaction summary for LRE run ID: {}", model.getRunId());
            Integer pcRunId = fetchPcRunId();
            if (pcRunId == null) {
                log.warn("No PC Run ID found for LRE run ID: {}", model.getRunId());
                return Collections.emptyList();
            }
            return fetchAllTransactions(pcRunId);
        } catch (Exception e) {
            log.error("Error while fetching transaction summary for run {}: {}",
                    model.getRunId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Integer fetchPcRunId() {
        try {
            String payload = String.format("{\"qcRunId\": %d}", model.getRunId());
            LreOpenRunDashboardResponse response = lreRestApis.getPCRunId(payload);
            Integer pcRunId = response.getData().getPcRunId();
            log.debug("Resolved PC Run ID: {}", pcRunId);
            return pcRunId;

        } catch (Exception e) {
            log.error("Failed to fetch PC Run ID for run {}: {}", model.getRunId(), e.getMessage());
            return null;
        }
    }

    private List<LreTransactionMetrics> fetchAllTransactions(int pcRunId) {
        List<LreTransactionMetrics> allTransactions = new ArrayList<>();
        int offset = 0;
        Integer totalAvailable;

        try {
            do {
                LreTransactionMetricsResponse response = fetchTransactionPage(pcRunId, offset);
                var perfData = response.getData();

                List<LreTransactionMetrics> page = perfData.getTransactions();
                totalAvailable = perfData.getTotalTransactions();

                if (page == null || page.isEmpty()) {
                    log.debug("No transactions found at offset {}", offset);
                    break;
                }

                allTransactions.addAll(page);
                log.debug("Fetched {} transactions at offset {}", page.size(), offset);

                offset += PAGE_SIZE;

            } while (totalAvailable != null && allTransactions.size() < totalAvailable);

        } catch (Exception e) {
            log.error("Error during paginated transaction fetch for PC Run {}: {}", pcRunId, e.getMessage(), e);
        }

        log.info("Completed fetching {} transactions for PC Run ID {}", allTransactions.size(), pcRunId);

        return allTransactions;
    }

    private LreTransactionMetricsResponse fetchTransactionPage(int pcRunId, int offset) {
        LreTransactionMetricsRequest request = new LreTransactionMetricsRequest(pcRunId, offset);
        String payload = JsonUtils.toJson(request);
        return lreRestApis.fetchTransactions(payload);
    }
}
