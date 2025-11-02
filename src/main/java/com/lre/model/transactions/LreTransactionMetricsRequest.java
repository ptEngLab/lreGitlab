package com.lre.model.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LreTransactionMetricsRequest {

    private Integer runId;
    private Integer offset;
    private Integer length = 50;
    private String sort = "Name ASC";
    private String filter = "";

    public LreTransactionMetricsRequest(int runId, int offset) {
        this.runId = runId;
        this.offset = offset;
    }
}
