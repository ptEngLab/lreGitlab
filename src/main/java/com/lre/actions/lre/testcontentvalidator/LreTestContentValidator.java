package com.lre.actions.lre.testcontentvalidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.runmodel.LreTestRunModel;
import com.lre.actions.utils.JsonUtils;
import com.lre.actions.utils.XmlUtils;
import com.lre.model.enums.LGDistributionType;
import com.lre.model.test.testcontent.MonitorProfile;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.lgdistribution.LGDistribution;
import com.lre.model.test.testcontent.workloadtype.WorkloadType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class LreTestContentValidator {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private final LreRestApis restApis;
    private final LreTestRunModel model;
    private TestContent content;

    public LreTestContentValidator(LreTestRunModel model, LreRestApis restApis) {
        this.model = model;
        this.restApis = restApis;
        content = getTestContentFromYaml();
    }


    public TestContent validateAndGetTestContent() {
        validateWorkloadType();
        validateLGDistribution();
        validateGroups();
        validateMonitorProfiles();

        log.info(XmlUtils.toXml(content));
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

    private void validateGroups(){
        LreScriptValidator scriptValidator = new LreScriptValidator(restApis, content);
        content = scriptValidator.validateGroups();
    }

    private void validateWorkloadType() {
        WorkloadType workloadType = WorkloadType.fromUserInput(content.getWorkloadTypeCode());
        content.setWorkloadType(workloadType);
        content.setWorkloadTypeCode(null); // We don't need to populate this for LRE API payload.
    }

    private void validateLGDistribution() {
        Integer lgAmount = content.getAmount();
        if (lgAmount == null) {
            content.setLgDistribution(new LGDistribution(LGDistributionType.MANUAL));
        } else if (lgAmount > 0) {
            content.setLgDistribution(new LGDistribution(LGDistributionType.ALL_TO_EACH_GROUP, content.getAmount()));
            content.setAmount(null); // We don't need to populate this for LRE API payload.
        } else {
            throw new LreException("Invalid LG amount: " + content.getAmount());
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
            content.setMonitorProfileId(null); // cleanup before API call
        }
    }


}
