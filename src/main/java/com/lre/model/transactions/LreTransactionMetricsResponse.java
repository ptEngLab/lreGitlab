package com.lre.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LreTransactionMetricsResponse {

    @JsonProperty("d")
    private LrePerformanceData data;

    @Data
    @NoArgsConstructor
    public static class LrePerformanceData {
        @JsonProperty("__type")
        private String type;

        @JsonProperty("Transactions")
        private List<LreTransactionMetrics> transactions;

        @JsonProperty("TotalTransactions")
        private Integer totalTransactions;
    }

}