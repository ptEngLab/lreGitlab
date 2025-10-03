package com.lre.actions.lre.testcontentvalidator.globalrts;

import com.lre.actions.lre.testcontentvalidator.rts.*;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.rts.RTS;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LreGlobalRtsValidator(TestContent content) {

    private static final LreRtsPacingValidator PACING_VALIDATOR = new LreRtsPacingValidator();
    private static final LreRtsThinkTimeValidator THINK_TIME_VALIDATOR = new LreRtsThinkTimeValidator();
    private static final LreRtsLogValidator LOG_VALIDATOR = new LreRtsLogValidator();
    private static final LreRtsJMeterValidator JMETER_VALIDATOR = new LreRtsJMeterValidator();
    private static final LreRtsSeleniumValidator SELENIUM_VALIDATOR = new LreRtsSeleniumValidator();
    private static final LreRtsJavaVmValidator JAVA_VM_VALIDATOR = new LreRtsJavaVmValidator();


    public void validateGlobalRts() {
        if (content.getGlobalRts() == null || content.getGlobalRts().isEmpty()) {
            log.debug("No Global RTS to validate in TestContent");
            return;
        }

        for (RTS globalRTS : content.getGlobalRts()) {
            globalRTS.setName(globalRTS.getYamlGlobalRtsName());
            PACING_VALIDATOR.validatePacingAndAttach(globalRTS, globalRTS.getYamlGlobalRtsPacing());
            THINK_TIME_VALIDATOR.validateThinkTimeAndAttach(globalRTS, globalRTS.getYamlGlobalRtsThinkTime());
            LOG_VALIDATOR.validateLogAndAttach(globalRTS, globalRTS.getYamlGlobalRtsLog());
            JMETER_VALIDATOR.validateJMeterAndAttach(globalRTS, globalRTS.getYamlGlobalRtsJmeter());
            SELENIUM_VALIDATOR.validateSeleniumAndAttach(globalRTS, globalRTS.getYamlGlobalRtsSelenium());
            JAVA_VM_VALIDATOR.validateJavaVmAndAttach(globalRTS, globalRTS.getYamlGlobalRtsJavaVM());

            cleanUpGlobalRtsYamlContentForApi(globalRTS);
        }

        log.debug("All groups validated successfully.");
    }


    private void cleanUpGlobalRtsYamlContentForApi(RTS globalRTS) {
        // Clear YAML-specific fields so they are not sent to API
        globalRTS.setYamlGlobalRtsName(null);
        globalRTS.setYamlGlobalRtsPacing(null);
        globalRTS.setYamlGlobalRtsThinkTime(null);
        globalRTS.setYamlGlobalRtsLog(null);
        globalRTS.setYamlGlobalRtsJmeter(null);
        globalRTS.setYamlGlobalRtsSelenium(null);
        globalRTS.setYamlGlobalRtsJavaVM(null);
    }
}
