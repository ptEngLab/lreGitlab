package com.lre.model.test.testcontent.groups.hosts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HostResponse {

    @JsonProperty("IsPrivateHost")
    private boolean isPrivateHost;

    @JsonProperty("ID")
    private int id;

    @JsonProperty("HostPurposes")
    private List<Integer> hostPurposes;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Source")
    private String source;

    @JsonProperty("Pools")
    private List<Integer> pools;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Installation")
    private int installation;

    @JsonProperty("Location")
    private String location;

    @JsonProperty("MIListener")
    private String miListener;

    @JsonProperty("Attributes")
    private List<Object> attributes;

    @JsonProperty("Priority")
    private int priority;

    @JsonProperty("SslEnabled")
    private boolean sslEnabled;

    @JsonProperty("Ipv6Enabled")
    private boolean ipv6Enabled;

    @JsonProperty("Domain")
    private String domain;

    @JsonProperty("UserName")
    private String userName;
}
