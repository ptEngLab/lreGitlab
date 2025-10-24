package com.lre.model.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class LreErrorResponse {
    @JsonProperty("ExceptionMessage")
    private String exceptionMessage;

    @JsonProperty("ErrorCode")
    private int errorCode;
}
