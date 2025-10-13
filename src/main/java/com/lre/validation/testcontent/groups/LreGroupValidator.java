package com.lre.validation.testcontent.groups;

import com.lre.actions.apis.LreRestApis;
import com.lre.actions.exceptions.LreException;
import com.lre.actions.utils.WorkloadUtils;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.commandline.CommandLine;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.yaml.YamlGroup;
import com.lre.model.yaml.YamlTest;
import com.lre.validation.testcontent.rts.*;
import com.lre.validation.testcontent.scheduler.SchedulerValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public record LreGroupValidator(LreRestApis restApis, TestContent content, YamlTest yamlTest) {

    private static final LreRtsPacingValidator PACING_VALIDATOR = new LreRtsPacingValidator();
    private static final LreRtsThinkTimeValidator THINK_TIME_VALIDATOR = new LreRtsThinkTimeValidator();
    private static final LreRtsLogValidator LOG_VALIDATOR = new LreRtsLogValidator();
    private static final LreRtsJMeterValidator JMETER_VALIDATOR = new LreRtsJMeterValidator();
    private static final LreRtsSeleniumValidator SELENIUM_VALIDATOR = new LreRtsSeleniumValidator();
    private static final LreRtsJavaVmValidator JAVA_VM_VALIDATOR = new LreRtsJavaVmValidator();

    public void validateGroups() {

        if (yamlTest.getGroups() == null || yamlTest.getGroups().isEmpty()) {
            throw new LreException("At least one group is required in your load test");
        }
        List<Group> validatedGroups = new ArrayList<>();

        for (YamlGroup yamlGroup : yamlTest.getGroups()) {
            Group group = new Group();
            group.setName(yamlGroup.getName());
            group.setScript(new LreGroupScriptValidator(restApis).validateYamlGroupScript(yamlGroup));
            group.setHosts(new LreGroupHostValidator(restApis, content).validateAndPopulateHosts(yamlGroup));

            if (StringUtils.isNotEmpty(yamlGroup.getGlobalCommandLine()))
                validateGlobalCommandLineReference(group, yamlGroup);

            if (StringUtils.isNotEmpty(yamlGroup.getGlobalRTS()))
                validateGlobalRtsReference(group, yamlGroup);
            else
                handleLocalRtsPerGroup(group, yamlGroup);

            group.setScheduler(validateGroupScheduler(yamlGroup));
            group.setVusers(yamlGroup.getVusers());
            validatedGroups.add(group);
        }


        content.setGroups(validatedGroups);

        log.debug("All groups validated successfully. Total groups: {}", validatedGroups.size());
    }


    private void handleLocalRtsPerGroup(Group group, YamlGroup yamlGroup) {
        RTS rts = new RTS();

        // Mandatory RTS settings – defaults are assigned inside the validators if not provided
        PACING_VALIDATOR.validatePacingAndAttach(rts, yamlGroup.getPacing());
        THINK_TIME_VALIDATOR.validateThinkTimeAndAttach(rts, yamlGroup.getThinkTime());
        LOG_VALIDATOR.validateLogAndAttach(rts, yamlGroup.getLog());

        // Optional RTS settings
        JMETER_VALIDATOR.validateJMeterAndAttach(rts, yamlGroup.getJmeter());
        SELENIUM_VALIDATOR.validateSeleniumAndAttach(rts, yamlGroup.getSelenium());
        JAVA_VM_VALIDATOR.validateJavaVmAndAttach(rts, yamlGroup.getJavaVM());

        group.setRts(rts);
    }


    private void validateGlobalRtsReference(Group group, YamlGroup yamlGroup) {
        List<RTS> globalRts = Optional.ofNullable(content.getGlobalRts()).orElse(Collections.emptyList());

        Optional<RTS> match = globalRts.stream()
                .filter(rts -> yamlGroup.getGlobalRTS().equalsIgnoreCase(rts.getName()))
                .findFirst();

        if (match.isEmpty()) {
            throw new LreException(String.format(
                    "Group '%s' references GlobalRTS '%s', but no such GlobalRTS is defined. Available GlobalRTS: %s",
                    yamlGroup.getName(),
                    yamlGroup.getGlobalRTS(),
                    globalRts.stream().map(RTS::getName).collect(Collectors.joining(", "))
            ));
        }

        group.setGlobalRTS(match.get().getName());

        log.debug("Group '{}' successfully bound to GlobalRTS '{}'", yamlGroup.getName(), yamlGroup.getGlobalRTS());
    }

    private void validateGlobalCommandLineReference(Group group, YamlGroup yamlGroup) {
        List<CommandLine> globalCmds = Optional.ofNullable(content.getGlobalCommandLines()).orElse(Collections.emptyList());

        Optional<CommandLine> match = globalCmds.stream()
                .filter(cl -> yamlGroup.getGlobalCommandLine().equalsIgnoreCase(cl.getName()))
                .findFirst();

        if (match.isEmpty()) {
            throw new LreException(String.format(
                    "Group '%s' references GlobalCommandLine '%s', but no such GlobalCommandLine is defined. Available GlobalCommandLines: %s",
                    yamlGroup.getName(),
                    yamlGroup.getGlobalCommandLine(),
                    globalCmds.stream().map(CommandLine::getName).collect(Collectors.joining(", "))
            ));
        }
        group.setCommandLine(match.get().getName());

        log.debug("Group '{}' successfully bound to GlobalCommandLine '{}'", yamlGroup.getName(), yamlGroup.getGlobalCommandLine());
    }


    private Scheduler validateGroupScheduler(YamlGroup yamlGroup) {
        String workloadType = content.getWorkloadType().getWorkloadTypeAsStr();

        if (workloadType.endsWith("group")) {
            List<Map<String, String>> schedulerData =
                    Optional.ofNullable(yamlGroup.getScheduler()).orElse(Collections.emptyList());
            int originalVusers = Optional.ofNullable(yamlGroup.getVusers()).orElse(0);

            Scheduler scheduler = new SchedulerValidator(content)
                    .validateScheduler(schedulerData, originalVusers);

            if (WorkloadUtils.isRealWorldByGroup(workloadType) && scheduler != null) {
                int totalStartVusers = scheduler.getActions().stream()
                        .filter(a -> a.getStartVusers() != null)
                        .mapToInt(a -> Optional.ofNullable(a.getStartVusers().getVusers()).orElse(0))
                        .sum();

                if (totalStartVusers > 0) {
                    if (originalVusers == 0) {
                        yamlGroup.setVusers(totalStartVusers);
                        log.debug("[Scheduler] Group '{}' had no vuser count, using cumulative {} from startVusers actions.",
                                yamlGroup.getName(), totalStartVusers);
                    } else if (originalVusers != totalStartVusers) {
                        log.warn("[Scheduler] Group '{}' defined {} vusers, but startVusers total {} differs. Overriding to {}.",
                                yamlGroup.getName(), originalVusers, totalStartVusers, totalStartVusers);
                        yamlGroup.setVusers(totalStartVusers);
                    } else {
                        log.debug("[Scheduler] Group '{}' vusers ({}) match cumulative startVusers count.",
                                yamlGroup.getName(), totalStartVusers);
                    }
                } else {
                    log.debug("[Scheduler] Group '{}' has no startVusers actions; keeping existing vusers count: {}",
                            yamlGroup.getName(), originalVusers);
                }
            }

            return scheduler;
        }

        return null;
    }



}
