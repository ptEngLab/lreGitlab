package com.lre.actions.lre.testcontentvalidator.groups;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.commandline.CommandLine;
import com.lre.model.test.testcontent.groups.rts.RTS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

@Slf4j
public record LreGroupValidator(LreRestApis restApis, TestContent content) {

    private static final LreRtsPacingValidator PACING_VALIDATOR = new LreRtsPacingValidator();
    private static final LreRtsThinkTimeValidator THINK_TIME_VALIDATOR = new LreRtsThinkTimeValidator();
    private static final LreRtsLogValidator LOG_VALIDATOR = new LreRtsLogValidator();
    private static final LreRtsJMeterValidator JMETER_VALIDATOR = new LreRtsJMeterValidator();
    private static final LreRtsSeleniumValidator SELENIUM_VALIDATOR = new LreRtsSeleniumValidator();
    private static final LreRtsJavaVmValidator JAVA_VM_VALIDATOR = new LreRtsJavaVmValidator();

    public void validateGroups() {
        if (content.getGroups() == null || content.getGroups().isEmpty()) {
            log.debug("No groups to validate in TestContent");
            return;
        }

        for (Group group : content.getGroups()) {
            // Script and host validations
            new LreGroupScriptValidator(restApis).validateAndSetScript(group);
            new LreGroupHostValidator(restApis, content).validateAndPopulateHosts(group);

            // === GlobalRTS handling ===
            if (StringUtils.isNotEmpty(group.getGlobalRTS())) {
                validateGlobalRtsReference(group);
            }

            // === GlobalCommandLine handling ===
            if (StringUtils.isNotEmpty(group.getGlobalCommandLine())) {
                validateGlobalCommandLineReference(group);
            }

            // === Local RTS handling ===
            RTS rts = getOrCreateRts(group);
            PACING_VALIDATOR.validatePacingAndAttach(rts, group.getYamlPacing());
            THINK_TIME_VALIDATOR.validateThinkTimeAndAttach(rts, group.getYamlThinkTime());
            LOG_VALIDATOR.validateLogAndAttach(rts, group.getYamlLog());
            JMETER_VALIDATOR.validateJMeterAndAttach(rts, group.getYamlJMeter());
            SELENIUM_VALIDATOR.validateSeleniumAndAttach(rts, group.getYamlSelenium());
            JAVA_VM_VALIDATOR.validateJavaVmAndAttach(rts, group.getYamlJavaVM());

            cleanUpGroupContentForApi(group);
        }

        log.debug("All groups validated successfully.");
    }

    /**
     * Ensures that GlobalRTS references in groups actually exist in TestContent.
     * Also warns if optional RTS configs will be ignored.
     */
    private void validateGlobalRtsReference(Group group) {
        boolean exists = content.getGlobalRts().stream()
                .anyMatch(rts -> group.getGlobalRTS().equalsIgnoreCase(rts.getName()));

        if (!exists) {
            throw new LreException(String.format(
                    "Group '%s' references GlobalRTS '%s', but no such GlobalRTS is defined. Available GlobalRTS: %s",
                    group.getName(),
                    group.getGlobalRTS(),
                    content.getGlobalRts().stream()
                            .map(RTS::getName)
                            .collect(Collectors.joining(", "))
            ));
        }

        if (StringUtils.isNotBlank(group.getYamlJMeter()) ||
                StringUtils.isNotBlank(group.getYamlSelenium()) ||
                StringUtils.isNotBlank(group.getYamlJavaVM())) {
            log.warn("Group '{}' references GlobalRTS '{}'. Local RTS definitions (JMeter, Selenium, JavaVM) will be ignored.",
                    group.getName(), group.getGlobalRTS());
        }

        log.debug("Group '{}' successfully bound to GlobalRTS '{}'", group.getName(), group.getGlobalRTS());
        cleanUpGroupContentForApi(group);
    }

    /**
     * Ensures that GlobalCommandLine references in groups actually exist in TestContent.
     * If the group also defines a local command line, warn and ignore it.
     */
    private void validateGlobalCommandLineReference(Group group) {
        boolean exists = content.getGlobalCommandLines().stream()
                .anyMatch(cl -> group.getGlobalCommandLine().equalsIgnoreCase(cl.getName()));

        if (!exists) {
            throw new LreException(String.format(
                    "Group '%s' references GlobalCommandLine '%s', but no such GlobalCommandLine is defined. Available GlobalCommandLines: %s",
                    group.getName(),
                    group.getGlobalCommandLine(),
                    content.getGlobalCommandLines().stream()
                            .map(CommandLine::getName)
                            .collect(Collectors.joining(", "))
            ));
        }

        if (StringUtils.isNotBlank(group.getCommandLine())) {
            log.warn("Group '{}' references GlobalCommandLine '{}'. Local command line '{}' will be ignored.",
                    group.getName(), group.getGlobalCommandLine(), group.getCommandLine());
        }

        log.debug("Group '{}' successfully bound to GlobalCommandLine '{}'", group.getName(), group.getGlobalCommandLine());
        cleanUpGroupContentForApi(group);
    }

    private void cleanUpGroupContentForApi(Group group) {
        // Clear YAML-only fields so they are not sent to API
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

    private RTS getOrCreateRts(Group group) {
        RTS rts = group.getRts();
        if (rts == null) group.setRts(rts = new RTS());
        return rts;
    }
}
