package com.lre.services.lre.calculation;

import com.lre.model.enums.SchedulerStartGroupType;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@UtilityClass
public class GroupOffsetCalculator {

    /**
     * Calculates when each group starts relative to test start time.
     *
     * @param timingInfo map of group name to timing information
     * @return map of group name to offset in seconds from test start
     */
    public static Map<String, Long> calculateOffsets(Map<String, GroupTimingInfo> timingInfo) {
        Map<String, Long> offsets = new HashMap<>();
        Set<String> processed = new HashSet<>();

        processImmediateGroups(timingInfo, offsets, processed);
        processDelayedGroups(timingInfo, offsets, processed);
        processDependentGroups(timingInfo, offsets, processed);

        // All groups should be processed by now (LRE guarantees valid configuration)
        return offsets;
    }

    private static void processImmediateGroups(Map<String, GroupTimingInfo> timingInfo,
                                               Map<String, Long> offsets,
                                               Set<String> processed) {
        timingInfo.forEach((groupName, info) -> {
            if (info.getStartGroup().getType() == SchedulerStartGroupType.IMMEDIATELY) {
                offsets.put(groupName, 0L);
                processed.add(groupName);
                log.debug("Group '{}': immediate start", groupName);
            }
        });
    }

    private static void processDelayedGroups(Map<String, GroupTimingInfo> timingInfo,
                                             Map<String, Long> offsets,
                                             Set<String> processed) {
        timingInfo.forEach((groupName, info) -> {
            if (!processed.contains(groupName) &&
                    info.getStartGroup().getType() == SchedulerStartGroupType.WITH_DELAY) {
                // TimeInterval is guaranteed for WITH_DELAY by LRE
                long delaySeconds = info.getStartGroup().getTimeInterval().toDuration().getSeconds();
                offsets.put(groupName, delaySeconds);
                processed.add(groupName);
                log.debug("Group '{}': delayed start by {}s", groupName, delaySeconds);
            }
        });
    }

    private static void processDependentGroups(Map<String, GroupTimingInfo> timingInfo,
                                               Map<String, Long> offsets,
                                               Set<String> processed) {
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, GroupTimingInfo> entry : timingInfo.entrySet()) {
                String groupName = entry.getKey();
                GroupTimingInfo info = entry.getValue();

                // Skip groups that are already processed or not dependent on another group
                if (processed.contains(groupName) ||
                        info.getStartGroup().getType() != SchedulerStartGroupType.WHEN_GROUP_FINISHES ||
                        !processed.contains(info.getStartGroup().getName())) {
                    continue;
                }

                String dependency = info.getStartGroup().getName();
                GroupTimingInfo depInfo = timingInfo.get(dependency);
                long depOffset = offsets.get(dependency);

                // Calculate when dependency group finishes
                long dependencyFinish = depOffset +
                        depInfo.getRampUpSeconds() +
                        depInfo.getDurationSeconds() +
                        depInfo.getRampDownSeconds();

                offsets.put(groupName, dependencyFinish);
                processed.add(groupName);
                changed = true;

                log.debug("Group '{}': starts when '{}' finishes at {}s",
                        groupName, dependency, dependencyFinish);
            }
        } while (changed && processed.size() < timingInfo.size());
    }

}