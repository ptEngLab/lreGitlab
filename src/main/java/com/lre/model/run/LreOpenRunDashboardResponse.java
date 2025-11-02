package com.lre.model.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LreOpenRunDashboardResponse {

    @JsonProperty("d")
    private RunResponseData data;

    @Data
    @NoArgsConstructor
    public static class RunResponseData {
        @JsonProperty("Message")
        private String message;

        @JsonProperty("PcRunId")
        private Integer pcRunId;

        @JsonProperty("RunName")
        private String runName;

        @JsonProperty("Success")
        private Boolean success;

        @JsonProperty("Online")
        private Boolean online;

        @JsonProperty("TestId")
        private Integer testId;
    }
}
