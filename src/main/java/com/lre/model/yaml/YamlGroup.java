package com.lre.model.yaml;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class YamlGroup {
    private String name;
    private Integer vusers;
    private String script;
    private String hostnames;
    private String hostTemplate;
    private List<Map<String, String>> scheduler;
    private String globalRTS;
    private String globalCommandLine;
    private String pacing;
    private String thinkTime;
    private String log;
    private String jmeter;
    private String selenium;
    private String javaVM;
}
