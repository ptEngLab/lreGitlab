package com.lre.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticationClient {

    @JsonProperty("ClientIdKey")
    private String clientIdKey;

    @JsonProperty("ClientSecretKey")
    private String clientSecretKey;
}
