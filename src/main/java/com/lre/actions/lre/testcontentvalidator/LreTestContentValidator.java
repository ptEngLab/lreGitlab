package com.lre.actions.lre.testcontentvalidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.lre.testcontentvalidator.globalrts.LreGlobalRtsValidator;
import com.lre.actions.lre.testcontentvalidator.groups.LreGroupValidator;
import com.lre.actions.lre.testcontentvalidator.scheduler.SchedulerValidator;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import com.lre.actions.utils.XmlUtils;
import com.lre.model.enums.LGDistributionType;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.hosts.HostResponse;
import com.lre.model.test.testcontent.lgdistribution.LGDistribution;
import com.lre.model.test.testcontent.monitorprofile.MonitorProfile;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.workloadtype.WorkloadType;
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

    public LreTestContentValidator(LreTestRunModel model, LreRestApis restApis) {
        this.model = model;
        this.restApis = restApis;
        content = getTestContentFromYaml();
    }


    public TestContent validateAndGetTestContent() {
        validateController();
        validateWorkloadType();
        validateLGDistribution();
        validateMonitorProfiles();
        validateGlobalRts();
        validateScheduler();
        validateGroups();

        cleanUpContentForApi();

        try (FileWriter writer = new FileWriter("Test.xml")) {
            writer.write(XmlUtils.toXml(content));
        } catch (IOException e) {
            log.error("Failed to write run id: {}", e.getMessage());
        }

        log.info(JsonUtils.toJson(content));
        return content;
    }

    private TestContent getTestContentFromYaml() {
        try {
            return YAML_MAPPER.readValue(model.getTestContentToCreate(), TestContent.class);
        } catch (JsonProcessingException e) {
            throw new LreException("Failed to parse test content YAML", e);
        }
    }

    private void validateController() {
        String controller = content.getController();
        if (StringUtils.isNotEmpty(controller)) {
            List<HostResponse> hosts = restApis.fetchControllers();
            boolean exists = hosts.stream().anyMatch(host -> controller.equalsIgnoreCase(host.getName()));
            if (exists) log.debug("Controller '{}' is available in LRE server", controller);
            else {
                String availableHosts = hosts.stream().map(HostResponse::getName).collect(Collectors.joining(", "));
                throw new LreException(String.format("Given Controller '%s' is not available on LRE. Expected one of: [%s]", controller, availableHosts));
            }
        }
    }


    private void validateGlobalRts() {
        new LreGlobalRtsValidator(content).validateGlobalRts();
    }

    private void validateGroups() {
        new LreGroupValidator(restApis, content).validateGroups();
    }

    private void validateScheduler() {
        List<String> schedulerItems = Optional.ofNullable(content.getSchedulerItems()).orElse(Collections.emptyList());
        Scheduler scheduler = new SchedulerValidator(content).validateScheduler(schedulerItems, getScenarioTotalVusers());
        content.setScheduler(scheduler);
    }

    private void validateWorkloadType() {
        WorkloadType workloadType = WorkloadType.fromUserInput(content.getWorkloadTypeCode());
        content.setWorkloadType(workloadType);
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
        Integer lgAmount = content.getLgAmount();
        if (lgAmount == null) {
            content.setLgDistribution(new LGDistribution(LGDistributionType.MANUAL));
        } else if (lgAmount > 0) {
            content.setLgDistribution(new LGDistribution(LGDistributionType.ALL_TO_EACH_GROUP, content.getLgAmount()));
        } else {
            throw new LreException("Invalid LG amount: " + content.getLgAmount());
        }
    }

    private void validateMonitorProfiles() {
        String ids = content.getMonitorProfileId();

        if (StringUtils.isNotBlank(ids)) {
            List<MonitorProfile> profiles =
                    Arrays.stream(ids.split(","))
                            .map(String::trim)
                            .filter(StringUtils::isNumeric)
                            .map(Integer::valueOf)
                            .map(MonitorProfile::new)
                            .toList();

            content.setMonitorProfiles(profiles);
        }
    }

    private void cleanUpContentForApi() {
        // clear all the custom variables used as part of YAML parsing to null, so that they are not sent for LRE API.
        content.setLgAmount(null);
        content.setWorkloadTypeCode(null);
        content.setMonitorProfileId(null);
        content.setSchedulerItems(null);
    }


}
