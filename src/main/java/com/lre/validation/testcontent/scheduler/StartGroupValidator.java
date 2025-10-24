package com.lre.validation.testcontent.scheduler;

import com.lre.model.enums.SchedulerStartGroupType;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.lre.common.constants.ConfigConstants.START_GROUP_DELAY_PATTERN;
import static com.lre.common.constants.ConfigConstants.START_GROUP_FINISH_PATTERN;
import static com.lre.model.test.testcontent.scheduler.action.common.TimeInterval.parseTimeInterval;

@Slf4j
public class StartGroupValidator {

    private final TestContent content;
    private final Set<String> normalizedGroupNames;
    private final List<Group> availableGroups;

    public StartGroupValidator(TestContent content) {
        this.content = content;
        this.availableGroups = content.getGroups();
        this.normalizedGroupNames = getNormalizedGroupNames();
    }

    public StartGroup validateGroup(String input) {
        StartGroup startGroup = new StartGroup(); // default: IMMEDIATELY

        if (StringUtils.isBlank(input)) {
            log.debug("[Scheduler] StartGroup input is blank → default type IMMEDIATELY");
            return startGroup;
        }

        String normalizedInput = input.trim().toLowerCase(Locale.ROOT);

        // Immediate start
        if (SchedulerStartGroupType.IMMEDIATELY.getValue().equalsIgnoreCase(normalizedInput)) {
            startGroup.setType(SchedulerStartGroupType.IMMEDIATELY);
            return startGroup;
        }

        // With delay
        Matcher delayMatcher = START_GROUP_DELAY_PATTERN.matcher(input);
        if (delayMatcher.matches()) {
            startGroup.setType(SchedulerStartGroupType.WITH_DELAY);
            startGroup.setTimeInterval(parseTimeInterval(delayMatcher.group("interval")));
            return startGroup;
        }

        // When another group finishes
        Matcher finishMatcher = START_GROUP_FINISH_PATTERN.matcher(input);
        if (finishMatcher.matches()) {
            String groupName = finishMatcher.group("groupName").trim();
            startGroup.setType(SchedulerStartGroupType.WHEN_GROUP_FINISHES);
            startGroup.setName(groupName);
            return startGroup;
        }

        // Fallback
        log.warn("[Scheduler] StartGroup input '{}' did not match any pattern → default type IMMEDIATELY", input);
        startGroup.setType(SchedulerStartGroupType.IMMEDIATELY);
        return startGroup;
    }

    public void validateAllGroupReferences(Scheduler scheduler) {
        if (scheduler == null || scheduler.getActions() == null) return;

        for (Action action : scheduler.getActions()) {
            if (action.getStartGroup() != null &&
                    action.getStartGroup().getType() == SchedulerStartGroupType.WHEN_GROUP_FINISHES) {

                String groupName = action.getStartGroup().getName();
                validateGroupExists(groupName);
            }
        }
    }

    private void validateGroupExists(String groupName) {
        if (!isValidGroup(groupName)) {
            throw new IllegalArgumentException("[Scheduler] Invalid group name in StartGroup: '" + groupName + "'. Available groups: " + availableGroupNamesAsStr());
        }
    }

    private boolean isValidGroup(String groupName) {
        return normalizedGroupNames.contains(groupName.toLowerCase(Locale.ROOT));
    }

    private Set<String> getNormalizedGroupNames() {
        if (availableGroups == null) return Set.of();
        return availableGroups.stream()
                .map(Group::getName)
                .filter(Objects::nonNull)
                .map(name -> name.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private String availableGroupNamesAsStr() {
        return availableGroups.stream().map(Group::getName).sorted().collect(Collectors.joining(", "));
    }

    public void validateStartGroupActions(List<Action> actions) {
        if (content.getWorkloadType().getWorkloadTypeAsStr().endsWith("group")) {
            List<Action> startGroups = actions.stream().filter(a -> a.getStartGroup() != null).toList();
            if (startGroups.isEmpty()) {
                log.warn("[Scheduler] No StartGroup found. Adding default StartGroup.");
                actions.add(0, Action.builder().startGroup(new StartGroup()).build());
            } else if (startGroups.size() > 1) {
                log.warn("[Scheduler] Multiple StartGroup actions found. Keeping first, removing others.");
                Action first = startGroups.get(0);
                actions.removeIf(a -> a.getStartGroup() != null && a != first);
            }
        }
    }

}
