package com.lre.validation.globalrts;

import com.lre.common.exceptions.LreException;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.yaml.YamlRTS;
import com.lre.model.yaml.YamlTest;
import com.lre.validation.rts.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public record LreGlobalRtsValidator(TestContent content, YamlTest yamlTest) {

    private static final LreRtsPacingValidator PACING_VALIDATOR = new LreRtsPacingValidator();
    private static final LreRtsThinkTimeValidator THINK_TIME_VALIDATOR = new LreRtsThinkTimeValidator();
    private static final LreRtsLogValidator LOG_VALIDATOR = new LreRtsLogValidator();
    private static final LreRtsJMeterValidator JMETER_VALIDATOR = new LreRtsJMeterValidator();
    private static final LreRtsSeleniumValidator SELENIUM_VALIDATOR = new LreRtsSeleniumValidator();
    private static final LreRtsJavaVmValidator JAVA_VM_VALIDATOR = new LreRtsJavaVmValidator();


    public void validateGlobalRts() {
        if (yamlTest.getGlobalRts() == null || yamlTest.getGlobalRts().isEmpty()) {
            log.debug("No Global RTS to validate in TestContent");
            return;
        }

        List<RTS> validatedRtsList = new ArrayList<>();


        for (YamlRTS yamlRts : yamlTest.getGlobalRts()) {
            if (StringUtils.isBlank(yamlRts.getName()))
                throw new LreException("Each GlobalRTS entry must have a unique 'name'");

            RTS rts = new RTS();
            rts.setName(yamlRts.getName());

            PACING_VALIDATOR.validatePacingAndAttach(rts, yamlRts.getPacing());
            THINK_TIME_VALIDATOR.validateThinkTimeAndAttach(rts, yamlRts.getThinkTime());
            LOG_VALIDATOR.validateLogAndAttach(rts, yamlRts.getLog());
            JMETER_VALIDATOR.validateJMeterAndAttach(rts, yamlRts.getJmeter());
            SELENIUM_VALIDATOR.validateSeleniumAndAttach(rts, yamlRts.getSelenium());
            JAVA_VM_VALIDATOR.validateJavaVmAndAttach(rts, yamlRts.getJavaVM());

            validatedRtsList.add(rts);
        }

        content.setGlobalRts(validatedRtsList);
        log.debug("Global RTS validation complete. Total validated: {}", validatedRtsList.size());
    }

}
