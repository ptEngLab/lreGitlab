package com.lre.model.test.testcontent.hostattributes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HostAttribute {

    @JsonProperty("HostName")
    private String hostName;

    @JsonProperty("LocationName")
    private String locationName;

    @JsonProperty("Attributes")
    private List<String> attributes;

}
