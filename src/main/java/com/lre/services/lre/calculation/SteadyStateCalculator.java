package com.lre.services.lre.calculation;

import com.lre.model.enums.*;
import com.lre.model.test.Test;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.common.VusersAction;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

import static com.lre.common.utils.CommonUtils.formatEpoch;
import static com.lre.model.enums.SchedulerVusersType.SIMULTANEOUSLY;

@Slf4j
public class SteadyStateCalculator {

    private final TestContent content;
    private final Test lreTest;
    private final String workloadTypeStr;

    public SteadyStateCalculator(Test lreTest) {
        this.lreTest = lreTest;
        this.content = lreTest.getContent();
        this.workloadTypeStr = content.getWorkloadType().getFullWorkloadTypeAsStr();
    }

    /**
     * Calculates steady-state results for the test.
     *
     * <p><strong>Note:</strong> For unsupported workload types (non-BASIC),
     * this method returns an empty list and logs a warning instead of throwing
     * an exception.</p>
     *
     * @param testStartTimeEpoch the epoch timestamp when the test starts
     * @return a list of {@link SteadyStateResult}, or empty list for unsupported types
     */
    public List<SteadyStateResult> calculateSteadyState(long testStartTimeEpoch) {
        WorkloadTypeEnum type = content.getWorkloadType().getType();
        WorkloadSubType subType = content.getWorkloadType().getSubType();

        if (type != WorkloadTypeEnum.BASIC) {
            log.warn("Unsupported workload type: {}. Returning empty steady state results.", type);
            return List.of(); // or return empty list
        }

        return subType == WorkloadSubType.BY_TEST
                ? List.of(calculateBasicByTest(testStartTimeEpoch))
                : calculateBasicByGroup(testStartTimeEpoch);
    }

    private SteadyStateResult calculateBasicByTest(long testStartTimeEpoch) {
        int totalUsers = calculateTotalUsers();
        Scheduler scheduler = content.getScheduler();

        StartVusers startVusers = getFirstAction(content.getScheduler().getActions(), Action::getStartVusers, "test");
        Duration duration = getFirstAction(scheduler.getActions(), Action::getDuration, "test");
        StopVusers stopVusers = getFirstAction(scheduler.getActions(), Action::getStopVusers, "test");

        long rampUpTime = calculateRampSeconds(totalUsers, startVusers);
        long durationSeconds = getDurationSeconds(duration);
        long rampDownTime = calculateRampSeconds(totalUsers, stopVusers);

        return buildResult(lreTest.getName(), totalUsers, rampUpTime, durationSeconds, rampDownTime,
                testStartTimeEpoch, 0L, null, null, null);    }

    private List<SteadyStateResult> calculateBasicByGroup(long testStartTimeEpoch) {
        List<Group> groups = content.getGroups();
        List<SteadyStateResult> results = new ArrayList<>(groups.size());

        Map<String, GroupTimingInfo> timingInfo = new HashMap<>();

        for (Group group : groups) {
            timingInfo.put(group.getName(), extractGroupTimingInfo(group));
        }

        Map<String, Long> startOffsets = calculateGroupStartOffsets(timingInfo);

        // Step 3: Build results
        for (Group group : groups) {
            String groupName = group.getName();
            GroupTimingInfo info = timingInfo.get(groupName);
            long offset = startOffsets.get(groupName);

            results.add(buildResult(
                    groupName,
                    info.getTotalUsers(),
                    info.getRampUpSeconds(),
                    info.getDurationSeconds(),
                    info.getRampDownSeconds(),
                    testStartTimeEpoch,
                    offset,
                    info.getStartGroup().getType(),
                    info.getStartGroup().getName(),
                    info.getDelaySeconds()
            ));
        }
/*        for (Group group : groups) {
            int groupUsers = group.getVusers();
            Scheduler scheduler = group.getScheduler();

            StartVusers startVusers = getFirstAction(group.getScheduler().getActions(), Action::getStartVusers, "group");

            long rampUpTime = calculateRampSeconds(groupUsers, startVusers);
            long duration = getDurationSeconds(group.getScheduler().getActions());
            results.add(buildResult(group.getName(), groupUsers, rampUpTime, duration, testStartTime));
        }*/

        return results;
    }


    /**
     * STEP 1: Extract timing information for a single group
     */
    private GroupTimingInfo extractGroupTimingInfo(Group group) {
        Scheduler scheduler = group.getScheduler();
        String groupName = group.getName();
        int totalUsers = group.getVusers();

        // Get all required actions from the group's scheduler
        StartGroup startGroup = getFirstAction(scheduler.getActions(), Action::getStartGroup, "group " + groupName);
        StartVusers startVusers = getFirstAction(scheduler.getActions(), Action::getStartVusers, "group " + groupName);
        Duration duration = getFirstAction(scheduler.getActions(), Action::getDuration, "group " + groupName);
        StopVusers stopVusers = getFirstAction(scheduler.getActions(), Action::getStopVusers, "group " + groupName);

        // Calculate timing metrics
        long rampUpSeconds = calculateRampSeconds(totalUsers, startVusers);
        long durationSeconds = getDurationSeconds(duration);
        long rampDownSeconds = calculateRampSeconds(totalUsers, stopVusers);

        // Extract delay if present
        Long delaySeconds = null;
        if (startGroup.getType() == SchedulerStartGroupType.WITH_DELAY && startGroup.getTimeInterval() != null) {
            delaySeconds = startGroup.getTimeInterval().toDuration().getSeconds();
        }

        return new GroupTimingInfo(
                groupName, totalUsers, startGroup, startVusers, duration, stopVusers,
                rampUpSeconds, durationSeconds, rampDownSeconds, delaySeconds
        );
    }

    /**
     * Generic method to get the first action of specified type from actions list
     * Since actions are guaranteed to exist, we can safely use this approach
     */
    private <T> T getFirstAction(List<Action> actions, Function<Action, T> extractor, String context) {
        return actions.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No action found for " + context));
    }


    /**
     * Get duration in seconds from actions list
     * Duration action is guaranteed to exist
     */
    private long getDurationSeconds(Duration duration) {
        if (duration.getType() != SchedulerDurationType.RUN_FOR) return 0L;
        TimeInterval timeInterval = duration.getTimeInterval();
        return timeInterval != null ? timeInterval.toDuration().getSeconds() : 0L;
    }

    /**
     * Optimized ramp calculation assuming guarantees
     * if startVusers.getType() is GRADUALLY,
     * Ramp is guaranteed with vusers > 0 and timeInterval with at least 1 sec
     */
    private long calculateRampSeconds(int totalUsers, VusersAction action) {
        if (action.getType() == SIMULTANEOUSLY) return 0L;
        Ramp ramp = action.getRamp();
        int batchSize = ramp.getVusers();
        if (totalUsers <= batchSize) return 0L;
        int intervals = (totalUsers - 1) / batchSize;
        return ramp.getTimeInterval().toDuration().multipliedBy(intervals).getSeconds();
    }


/*    private SteadyStateResult buildResult(String name, int totalUsers, long rampUpTime,
                                          long durationSeconds, long testStartTime) {
        long steadyStateStart = testStartTime + rampUpTime;
        long steadyStateEnd = durationSeconds > 0 ? testStartTime + durationSeconds : 0;

        log.info("{} steady state: start: {}, end: {}, rampUp: {}s ({}), duration: {}s",
                name, steadyStateStart, steadyStateEnd, rampUpTime,
                formatDuration(rampUpTime * 1000), durationSeconds);

        return SteadyStateResult.builder()
                .groupName(name)
                .totalUsers(totalUsers)
                .rampUpTimeSeconds(rampUpTime)
                .steadyStateDurationSeconds(durationSeconds)
                .steadyStateStartEpoch(steadyStateStart)
                .steadyStateEndEpoch(steadyStateEnd)
                .workloadType(workloadTypeStr)
                .build();
    }*/

    private SteadyStateResult buildResult(String name, int totalUsers,
                                          long rampUpSeconds, long durationSeconds, long rampDownSeconds,
                                          long testStartTimeEpoch, long groupStartOffsetSeconds,
                                          SchedulerStartGroupType startGroupType,
                                          String dependencyGroup,
                                          Long delaySeconds) {

        // ========== CALCULATE ALL TIMES ==========

        // Total group running time
        long groupTotalTime = rampUpSeconds + durationSeconds + rampDownSeconds;

        // Relative to group start (seconds after group starts)
        // Steady state starts after ramp up
        long relativeSteadyEnd = rampUpSeconds + durationSeconds;  // Steady state ends before ramp down

        // Relative to test start (seconds after test starts)
        long relativeToTestStart = groupStartOffsetSeconds + rampUpSeconds;
        long relativeToTestEnd = groupStartOffsetSeconds + rampUpSeconds + durationSeconds;
        long relativeGroupEnd = groupStartOffsetSeconds + groupTotalTime;

        // Absolute epoch times
        long absoluteGroupStart = testStartTimeEpoch + groupStartOffsetSeconds;
        long absoluteSteadyStart = absoluteGroupStart + rampUpSeconds;
        long absoluteSteadyEnd = durationSeconds > 0 ? absoluteGroupStart + rampUpSeconds + durationSeconds : 0;
        long absoluteGroupEnd = absoluteGroupStart + groupTotalTime;

        // ========== LOGGING ==========
        log.info("=== Group '{}' ===", name);
        log.info("StartGroup Type: {}", startGroupType);
        if (dependencyGroup != null) {
            log.info("Depends on: {}", dependencyGroup);
        }
        if (delaySeconds != null) {
            log.info("Delay: {}s", delaySeconds);
        }
        log.info("Offset from test start: {}s", groupStartOffsetSeconds);
        log.info("Timing - Ramp Up: {}s, Steady: {}s, Ramp Down: {}s, Total: {}s",
                rampUpSeconds, durationSeconds, rampDownSeconds, groupTotalTime);
        log.info("Relative to test start:");
        log.info("  - Group starts: {}s", groupStartOffsetSeconds);
        log.info("  - Steady state: {}s to {}s", relativeToTestStart, relativeToTestEnd);
        log.info("  - Group ends: {}s", relativeGroupEnd);
        log.info("Absolute times:");
        log.info("  - Group starts: {} ({})", absoluteGroupStart, formatEpoch(absoluteGroupStart));
        log.info("  - Steady state: {} to {} ({})", absoluteSteadyStart, absoluteSteadyEnd, formatEpoch(absoluteSteadyStart));
        if (absoluteSteadyEnd > 0) log.info("                           ({})", formatEpoch(absoluteSteadyEnd));


        // ========== BUILD RESULT ==========
        return SteadyStateResult.builder()
                .groupName(name)
                .totalUsers(totalUsers)
                .workloadType(workloadTypeStr)

                // StartGroup configuration
                .startGroupType(startGroupType)
                .dependencyGroupName(dependencyGroup)
                .delaySeconds(delaySeconds)

                // Timing metrics
                .rampUpTimeSeconds(rampUpSeconds)
                .steadyStateDurationSeconds(durationSeconds)
                .rampDownTimeSeconds(rampDownSeconds)
                .groupTotalTimeSeconds(groupTotalTime)

                // Offsets
                .groupStartOffsetSeconds(groupStartOffsetSeconds)

                // Relative to group start
                .relativeToGroupStartSeconds(rampUpSeconds)
                .relativeToGroupEndSeconds(relativeSteadyEnd)

                // Relative to test start (what you asked for)
                .relativeToTestStartSeconds(relativeToTestStart)
                .relativeToTestEndSeconds(relativeToTestEnd)
                .relativeGroupEndSeconds(relativeGroupEnd)

                // Absolute epoch times
                .absoluteTestStartEpoch(testStartTimeEpoch)
                .absoluteGroupStartEpoch(absoluteGroupStart)
                .absoluteSteadyStateStartEpoch(absoluteSteadyStart)
                .absoluteSteadyStateEndEpoch(absoluteSteadyEnd)
                .absoluteGroupEndEpoch(absoluteGroupEnd)

                .build();
    }

    private int calculateTotalUsers() {
        WorkloadVusersDistributionMode mode = content.getWorkloadType().getVusersDistributionMode();
        if (mode == WorkloadVusersDistributionMode.BY_PERCENTAGE) return content.getTotalVusers();
        else {  // BY_NUMBER
            int sum = 0;
            for (Group group : content.getGroups()) sum += Optional.ofNullable(group.getVusers()).orElse(0);
            return sum;
        }
    }

    /**
     * STEP 2: Calculate when each group starts
     */
    private Map<String, Long> calculateGroupStartOffsets(Map<String, GroupTimingInfo> timingInfo) {
        Map<String, Long> offsets = new HashMap<>();
        Set<String> processed = new HashSet<>();

        // Phase 1: Process immediate groups
        for (Map.Entry<String, GroupTimingInfo> entry : timingInfo.entrySet()) {
            String groupName = entry.getKey();
            StartGroup startGroup = entry.getValue().getStartGroup();

            if (startGroup.getType() == SchedulerStartGroupType.IMMEDIATELY) {
                offsets.put(groupName, 0L);
                processed.add(groupName);
                log.debug("Group '{}': immediate start (offset=0s)", groupName);
            }
        }

        // Phase 2: Process groups with delay
        for (Map.Entry<String, GroupTimingInfo> entry : timingInfo.entrySet()) {
            String groupName = entry.getKey();
            if (processed.contains(groupName)) continue;

            StartGroup startGroup = entry.getValue().getStartGroup();
            if (startGroup.getType() == SchedulerStartGroupType.WITH_DELAY) {
                TimeInterval delay = startGroup.getTimeInterval();
                long delaySeconds = delay != null ? delay.toDuration().getSeconds() : 0L;
                offsets.put(groupName, delaySeconds);
                processed.add(groupName);
                log.debug("Group '{}': delayed start (offset={}s)", groupName, delaySeconds);
            }
        }

        // Phase 3: Process dependent groups (WHEN_GROUP_FINISHES)
        boolean changed;
        do {
            changed = false;

            for (Map.Entry<String, GroupTimingInfo> entry : timingInfo.entrySet()) {
                String groupName = entry.getKey();
                if (processed.contains(groupName)) continue;

                StartGroup startGroup = entry.getValue().getStartGroup();
                if (startGroup.getType() != SchedulerStartGroupType.WHEN_GROUP_FINISHES) {
                    continue;
                }

                String dependency = startGroup.getName();
                if (!processed.contains(dependency)) {
                    continue; // Dependency not ready yet
                }

                // Calculate when dependency finishes
                GroupTimingInfo depInfo = timingInfo.get(dependency);
                Long depOffset = offsets.get(dependency);

                // Dependency finishes at: start + rampUp + duration + rampDown
                long dependencyFinish = depOffset +
                        depInfo.getRampUpSeconds() +
                        depInfo.getDurationSeconds() +
                        depInfo.getRampDownSeconds();

                offsets.put(groupName, dependencyFinish);
                processed.add(groupName);
                changed = true;

                log.debug("Group '{}': starts when '{}' finishes (offset={}s)",
                        groupName, dependency, dependencyFinish);
            }

        } while (changed && processed.size() < timingInfo.size());

        // Handle any unprocessed groups (shouldn't happen with valid config)
        for (String groupName : timingInfo.keySet()) {
            if (!processed.contains(groupName)) {
                log.warn("Group '{}': could not determine start time, using immediate", groupName);
                offsets.put(groupName, 0L);
            }
        }

        return offsets;
    }


    @Data
    @AllArgsConstructor
    private static class GroupTimingInfo {
        private String groupName;
        private int totalUsers;
        private StartGroup startGroup;
        private StartVusers startVusers;
        private Duration duration;
        private StopVusers stopVusers;
        private long rampUpSeconds;
        private long durationSeconds;
        private long rampDownSeconds;
        private Long delaySeconds;
    }
}