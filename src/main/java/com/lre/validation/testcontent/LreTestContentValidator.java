package com.lre.validation.testcontent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lre.client.api.lre.LreRestApis;
import com.lre.common.exceptions.LreException;
import com.lre.client.runmodel.LreTestRunModel;
import com.lre.common.utils.XmlUtils;
import com.lre.model.enums.LGDistributionType;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.analysistemplate.AnalysisTemplate;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.commandline.CommandLine;
import com.lre.model.test.testcontent.groups.hosts.HostResponse;
import com.lre.model.test.testcontent.lgdistribution.LGDistribution;
import com.lre.model.test.testcontent.monitorofw.MonitorOFW;
import com.lre.model.test.testcontent.monitorprofile.MonitorProfile;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.workloadtype.WorkloadType;
import com.lre.model.yaml.YamlCommandLine;
import com.lre.model.yaml.YamlTest;
import com.lre.validation.globalrts.LreGlobalRtsValidator;
import com.lre.validation.groups.LreGroupValidator;
import com.lre.validation.scheduler.SchedulerValidator;
import com.lre.validation.sla.SLAValidator;
import com.lre.validation.trending.AutomaticTrendingValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LreTestContentValidator {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private final LreRestApis restApis;
    private final LreTestRunModel model;
    private final TestContent content;
    private final YamlTest yamlTest;

    private static final String CONTROLLER_NOT_FOUND =
            "Given Controller '%s' is not available on LRE. Expected one of: [%s]";

    public LreTestContentValidator(LreTestRunModel model, LreRestApis restApis) {
        this.model = model;
        this.restApis = restApis;
        content = new TestContent();
        yamlTest = getTestContentFromYaml();
    }


    public TestContent buildTestContent() {
        validateController();
        validateWorkloadType();
        validateLGDistribution();
        validateMonitorProfiles();
        validateMonitorsOverFirewall();
        validateGlobalRts();
        validateGlobalCommandLines();
        validateGlobalScheduler();
        validateGroups();
        validateAnalysisTemplate();
        validateAutomaticTrendReportData();
        validateSLA();


        try (FileWriter writer = new FileWriter("Test.xml")) {
            writer.write(XmlUtils.toXml(content));
        } catch (IOException e) {
            log.error("Failed to write run id: {}", e.getMessage());
        }

        return content;
    }


    private YamlTest getTestContentFromYaml() {
        try {
            return YAML_MAPPER.readValue(model.getTestContentToCreate(), YamlTest.class);
        } catch (JsonProcessingException e) {
            throw new LreException("Failed to parse test content YAML", e);
        }
    }

    private void validateController() {
        String controller = yamlTest.getController();
        if (StringUtils.isNotEmpty(controller)) {
            List<HostResponse> hosts = restApis.fetchControllers();
            boolean exists = hosts.stream().anyMatch(host -> controller.equalsIgnoreCase(host.getName()));
            if (exists) log.debug("Controller '{}' is available in LRE server", controller);
            else {
                String availableHosts = hosts.stream().map(HostResponse::getName).collect(Collectors.joining(", "));
                throw new LreException(String.format(CONTROLLER_NOT_FOUND, controller, availableHosts));
            }
        }

        content.setController(controller);
    }

    private void validateGlobalRts() {
        new LreGlobalRtsValidator(content, yamlTest).validateGlobalRts();
    }

    private void validateGlobalCommandLines() {
        List<YamlCommandLine> yamlCommandLines = Optional.ofNullable(yamlTest.getGlobalCommandLines())
                .orElse(Collections.emptyList());

        List<CommandLine> domainCommandLines = yamlCommandLines.stream()
                .map(this::convertToDomainCommandLine)
                .collect(Collectors.toList());

        content.setGlobalCommandLines(domainCommandLines);
    }

    private CommandLine convertToDomainCommandLine(YamlCommandLine yamlCmd) {
        CommandLine commandLine = new CommandLine();
        commandLine.setName(yamlCmd.getName());
        commandLine.setValue(yamlCmd.getValue());
        return commandLine;
    }

    private void validateGroups() {
        new LreGroupValidator(restApis, content, yamlTest).validateGroups();
    }

    private void validateSLA() {
        new SLAValidator(content, yamlTest).validateSLA();
    }

    private void validateGlobalScheduler() {
        String workloadType = content.getWorkloadType().getWorkloadTypeAsStr();
        if (workloadType.endsWith("group")) {
            content.setScheduler(null);
        } else {
            List<Map<String, String>> schedulerItems = Optional.ofNullable(yamlTest.getScheduler())
                    .orElse(Collections.emptyList());
            SchedulerValidator schedulerValidator = new SchedulerValidator(content);
            Scheduler scheduler = schedulerValidator.validateScheduler(schedulerItems, getScenarioTotalVusers());
            content.setScheduler(scheduler);

        }
    }


    private void validateWorkloadType() {
        WorkloadType workloadType = WorkloadType.fromUserInput(yamlTest.getWorkloadTypeCode());
        content.setWorkloadType(workloadType);
    }

    private void validateAutomaticTrendReportData() {
        new AutomaticTrendingValidator(content, yamlTest).validateAutomaticTrending();

    }

    private int getScenarioTotalVusers() {
        return Optional.ofNullable(content.getGroups())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(Group::getVusers)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private void validateLGDistribution() {
        Integer lgAmount = yamlTest.getLgAmount();
        if (lgAmount == null) {
            content.setLgDistribution(new LGDistribution(LGDistributionType.MANUAL));
        } else if (lgAmount > 0) {
            content.setLgDistribution(new LGDistribution(LGDistributionType.ALL_TO_EACH_GROUP, yamlTest.getLgAmount()));
        } else {
            throw new LreException("Invalid LG amount: " + yamlTest.getLgAmount());
        }
    }

    private void validateMonitorProfiles() {
        List<Integer> ids = yamlTest.getMonitorProfileIds();

        if (ids != null && !ids.isEmpty()) {
            List<MonitorProfile> profiles = ids.stream()
                    .filter(Objects::nonNull)
                    .map(MonitorProfile::new)
                    .toList();

            content.setMonitorProfiles(profiles);
        }
    }


    private void validateMonitorsOverFirewall() {
        String ids = yamlTest.getMonitorOFWId();

        if (StringUtils.isNotBlank(ids)) {
            List<MonitorOFW> profiles =
                    Arrays.stream(ids.split(","))
                            .map(String::trim)
                            .filter(StringUtils::isNumeric)
                            .map(Integer::valueOf)
                            .map(MonitorOFW::new)
                            .toList();

            content.setMonitorOFWIds(profiles);
        }
    }

    private void validateAnalysisTemplate() {
        String analysisTemplateId = yamlTest.getAnalysisTemplateId();
        if (StringUtils.isNotBlank(analysisTemplateId)) {
            AnalysisTemplate analysisTemplate = new AnalysisTemplate();
            analysisTemplate.setId(Integer.parseInt(analysisTemplateId));
            content.setAnalysisTemplate(analysisTemplate);
        }
    }

}
