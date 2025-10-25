package com.lre.validation.groups;

import com.lre.client.api.lre.LreRestApis;
import com.lre.common.exceptions.LreException;
import com.lre.common.utils.WorkloadUtils;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.commandline.CommandLine;
import com.lre.model.test.testcontent.groups.rts.RTS;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.yaml.YamlGroup;
import com.lre.model.yaml.YamlTest;
import com.lre.validation.rts.*;
import com.lre.validation.scheduler.SchedulerValidator;
import com.lre.validation.scheduler.StartGroupValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public record LreGroupValidator(LreRestApis restApis, TestContent content, YamlTest yamlTest) {
    private static final int DEFAULT_VUSERS = 1;

    public void validateGroups() {
        if (yamlTest.getGroups() == null || yamlTest.getGroups().isEmpty()) {
            throw new LreException("At least one group is required in your load test");
        }

        List<Group> validatedGroups = yamlTest.getGroups().stream()
                .map(yamlGroup -> new GroupBuilder(yamlGroup).build())
                .toList();

        content.setGroups(validatedGroups);
        validateAllGroupDependencies();

        log.debug("All groups validated successfully. Total groups: {}", validatedGroups.size());
    }

    private void validateAllGroupDependencies() {
        StartGroupValidator groupValidator = new StartGroupValidator(content);
        for (Group group : content.getGroups()) {
            if (group.getScheduler() != null) {
                groupValidator.validateAllGroupReferences(group.getScheduler());
            }
        }
    }

    private class GroupBuilder {
        private final YamlGroup yamlGroup;
        private final Group group = new Group();

        GroupBuilder(YamlGroup yamlGroup) {
            this.yamlGroup = yamlGroup;
        }

        Group build() {
            group.setName(yamlGroup.getName());
            group.setScript(new LreGroupScriptValidator(restApis).validateYamlGroupScript(yamlGroup));
            group.setHosts(new LreGroupHostValidator(restApis, content).validateAndPopulateHosts(yamlGroup));

            resolveCommandLine();
            resolveRts();
            group.setScheduler(resolveScheduler());
            group.setVusers(resolveVusers());

            return group;
        }

        private void resolveCommandLine() {
            if (StringUtils.isEmpty(yamlGroup.getGlobalCommandLine())) return;

            List<CommandLine> globalCmds = Optional.ofNullable(content.getGlobalCommandLines()).orElse(Collections.emptyList());
            String ref = yamlGroup.getGlobalCommandLine();

            globalCmds.stream()
                    .filter(cl -> ref.equalsIgnoreCase(cl.getName()))
                    .findFirst()
                    .ifPresentOrElse(
                            cl -> {
                                group.setCommandLine(cl.getName());
                                log.debug("Group '{}' bound to GlobalCommandLine '{}'", yamlGroup.getName(), ref);
                            },
                            () -> {
                                throw new LreException(String.format(
                                        "Group '%s' references GlobalCommandLine '%s', but it's not defined. Available: %s",
                                        yamlGroup.getName(), ref,
                                        globalCmds.stream().map(CommandLine::getName).collect(Collectors.joining(", "))
                                ));
                            }
                    );
        }

        private void resolveRts() {
            if (StringUtils.isNotEmpty(yamlGroup.getGlobalRTS())) {
                bindGlobalRts();
            } else {
                group.setRts(buildLocalRts());
            }
        }

        private void bindGlobalRts() {
            List<RTS> globalRts = Optional.ofNullable(content.getGlobalRts()).orElse(Collections.emptyList());
            String ref = yamlGroup.getGlobalRTS();

            globalRts.stream()
                    .filter(rts -> ref.equalsIgnoreCase(rts.getName()))
                    .findFirst()
                    .ifPresentOrElse(
                            rts -> {
                                group.setGlobalRTS(rts.getName());
                                log.debug("Group '{}' bound to GlobalRTS '{}'", yamlGroup.getName(), ref);
                            },
                            () -> {
                                throw new LreException(String.format(
                                        "Group '%s' references GlobalRTS '%s', but it's not defined. Available: %s",
                                        yamlGroup.getName(), ref,
                                        globalRts.stream().map(RTS::getName).collect(Collectors.joining(", "))
                                ));
                            }
                    );
        }

        private RTS buildLocalRts() {
            RTS rts = new RTS();
            new LreRtsPacingValidator().validatePacingAndAttach(rts, yamlGroup.getPacing());
            new LreRtsThinkTimeValidator().validateThinkTimeAndAttach(rts, yamlGroup.getThinkTime());
            new LreRtsLogValidator().validateLogAndAttach(rts, yamlGroup.getLog());
            new LreRtsJMeterValidator().validateJMeterAndAttach(rts, yamlGroup.getJmeter());
            new LreRtsSeleniumValidator().validateSeleniumAndAttach(rts, yamlGroup.getSelenium());
            new LreRtsJavaVmValidator().validateJavaVmAndAttach(rts, yamlGroup.getJavaVM());
            return rts;
        }

        private Scheduler resolveScheduler() {
            String workloadType = content.getWorkloadType().getWorkloadTypeAsStr();
            if (!workloadType.endsWith("group")) return null;

            List<Map<String, String>> schedulerData = Optional.ofNullable(yamlGroup.getScheduler()).orElse(Collections.emptyList());
            int originalVusers = Optional.ofNullable(yamlGroup.getVusers()).orElse(0);

            Scheduler scheduler = new SchedulerValidator(content).validateScheduler(schedulerData, originalVusers);

            if (WorkloadUtils.isRealWorldByGroup(workloadType) && scheduler != null) {
                int totalStartVusers = scheduler.getActions().stream()
                        .filter(a -> a.getStartVusers() != null)
                        .mapToInt(a -> Optional.ofNullable(a.getStartVusers().getVusers()).orElse(0))
                        .sum();

                if (totalStartVusers > 0) {
                    if (originalVusers != totalStartVusers) {
                        log.warn("[Scheduler] Group '{}' vusers mismatch: defined={}, startVusers={}. Overriding.",
                                yamlGroup.getName(), originalVusers, totalStartVusers);
                    } else {
                        log.debug("[Scheduler] Group '{}' vusers match startVusers total: {}", yamlGroup.getName(), totalStartVusers);
                    }
                    yamlGroup.setVusers(totalStartVusers);
                    group.setVusers(totalStartVusers);
                }
            }

            return scheduler;
        }

        private int resolveVusers() {
            String workloadType = content.getWorkloadType().getWorkloadTypeAsStr();
            Integer yamlVusers = yamlGroup.getVusers();

            if (WorkloadUtils.isRealWorldByGroup(workloadType)) {
                log.debug("Group '{}' is RealWorldByGroup. Vusers derived from scheduler.", yamlGroup.getName());
                return 0; // Scheduler will dictate vusers
            }

            if (yamlVusers != null && yamlVusers > 0) {
                log.debug("Group '{}' defines {} Vusers.", yamlGroup.getName(), yamlVusers);
                return yamlVusers;
            }

            log.warn("Group '{}' has no Vusers defined. Using default: {}", yamlGroup.getName(), DEFAULT_VUSERS);
            return DEFAULT_VUSERS;
        }
    }
}
