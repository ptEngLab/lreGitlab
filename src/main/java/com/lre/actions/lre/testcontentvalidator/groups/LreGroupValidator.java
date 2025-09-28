package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.actions.apis.LreRestApis;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public record LreGroupValidator(LreRestApis restApis, TestContent content) {

    public void validateGroups() {
        if (content.getGroups() == null || content.getGroups().isEmpty()) {
            log.info("No groups to validate in TestContent");
            return;
        }


        for (Group group : content.getGroups()) {
            new LreGroupScriptValidator(restApis).validateAndSetScript(group);     // updates script directly
            new LreGroupHostValidator(restApis, content).validateAndPopulateHosts(group);  // updates hosts directly
            new LreRtsPacingValidator().validatePacingForGroup(group);
            new LreRtsThinkTimeValidator().validateThinkTimeForGroup(group);
            new LreRtsLogValidator().validateLogForGroup(group);
            new LreRtsJMeterValidator().validateJMeterForGroup(group);
            new LreRtsSeleniumValidator().validateSeleniumForGroup(group);
            new LreRtsJavaVmValidator().validateJavaVmForGroup(group);

            cleanUpGroupContentForApi(group);
        }

        log.debug("All groups validated successfully.");
    }

    private void cleanUpGroupContentForApi(Group group) {

        // clear all the custom variables used as part of YAML parsing to null, so that they are not sent for LRE API.
        group.setYamlScriptId(null);
        group.setYamlScriptName(null);
        group.setYamlHostname(null);
        group.setYamlHostTemplate(null);
        group.setYamlPacing(null);
        group.setYamlThinkTime(null);
        group.setYamlLog(null);
        group.setYamlJMeter(null);
        group.setYamlSelenium(null);
        group.setYamlJavaVM(null);
    }


}
