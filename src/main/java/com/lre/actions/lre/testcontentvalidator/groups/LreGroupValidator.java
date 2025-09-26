package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.actions.apis.LreRestApis;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LreGroupValidator {
    private final LreRestApis restApis;
    private final TestContent content;

    public LreGroupValidator(LreRestApis restApis, TestContent content) {
        this.restApis = restApis;
        this.content = content;
    }

    public void validateGroups() {
        if (content.getGroups() == null || content.getGroups().isEmpty()) {
            log.info("No groups to validate in TestContent");
            return;
        }


        for (Group group : content.getGroups()) {
            new LreScriptValidator(restApis).validateAndSetScript(group);     // updates script directly
            new LreHostValidator(restApis, content).validateAndPopulateHosts(group);  // updates hosts directly
            new LrePacingValidator().validatePacingForGroup(group);
            new LreThinkTimeValidator().validateThinkTimeForGroup(group);

            cleanUpGroupContentForApi(group);
        }

        log.info("All groups validated successfully.");
    }

    private void cleanUpGroupContentForApi(Group group) {

        // clear all the custom variables used as part of YAML parsing to null, so that they are not sent for LRE API.
        group.setScriptId(null);
        group.setScriptName(null);
        group.setHostname(null);
        group.setHostTemplate(null);
        group.setPacing(null);
        group.setThinkTime(null);
    }


}
