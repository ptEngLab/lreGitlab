package com.lre.model.yaml;

import com.lre.model.test.testcontent.sla.SLAConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class YamlTest {
    private String controller;
    private Integer lgAmount;
    private Integer workloadTypeCode;
    private List<Integer> monitorProfileIds;
    private String monitorOFWId;
    private String analysisTemplateId;
    private List<String> scheduler;
    private SLAConfig sla;
    private List<YamlRTS> globalRts;
    private List<YamlCommandLine> globalCommandLines;
    private YamlAutomaticTrending automaticTrending;
    private List<YamlGroup> groups = new ArrayList<>();
}
