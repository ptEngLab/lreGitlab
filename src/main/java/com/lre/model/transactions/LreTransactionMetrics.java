package com.lre.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LreTransactionMetrics {

    @JsonProperty("_stopped")
    private Integer internalStopped;

    @JsonProperty("Failed")
    private Integer failedCount;

    @JsonProperty("Passed")
    private Integer passedCount;

    @JsonProperty("Stopped")
    private Integer stoppedCount;

    @JsonProperty("Name")
    private String  transactionName;

    @JsonProperty("TPS")
    private Double tps;

    @JsonProperty("SuccessRate")
    private Integer successRatePercentage;

}
