package com.lre.services.lre.calculation;

import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.enums.WorkloadSubType;
import com.lre.model.enums.WorkloadTypeEnum;
import com.lre.model.enums.WorkloadVusersDistributionMode;
import com.lre.model.test.Test;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.lre.common.utils.CommonUtils.formatDuration;
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
     * @param testStartTime the epoch timestamp when the test starts
     * @return a list of {@link SteadyStateResult}, or empty list for unsupported types
     */
    public List<SteadyStateResult> calculateSteadyState(long testStartTime) {
        WorkloadTypeEnum type = content.getWorkloadType().getType();
        WorkloadSubType subType = content.getWorkloadType().getSubType();

        if (type != WorkloadTypeEnum.BASIC) {
            log.warn("Unsupported workload type: {}. Returning empty steady state results.", type);
            return List.of(); // or return empty list
        }

        return subType == WorkloadSubType.BY_TEST
                ? List.of(calculateBasicByTest(testStartTime))
                : calculateBasicByGroup(testStartTime);
    }

    private SteadyStateResult calculateBasicByTest(long testStartTime) {
        int totalUsers = calculateTotalUsers();
        StartVusers startVusers = getFirstAction(content.getScheduler().getActions(), Action::getStartVusers, "test");
        long rampUpTime = calculateRampUpSeconds(totalUsers, startVusers);
        long duration = getDurationSeconds(content.getScheduler().getActions());
        return buildResult(lreTest.getName(), totalUsers, rampUpTime, duration, testStartTime);
    }

    private List<SteadyStateResult> calculateBasicByGroup(long testStartTime) {
        List<Group> groups = content.getGroups();
        List<SteadyStateResult> results = new ArrayList<>(groups.size());

        for (Group group : groups) {
            int groupUsers = group.getVusers();
            StartVusers startVusers = getFirstAction(group.getScheduler().getActions(), Action::getStartVusers, "group");
            long rampUpTime = calculateRampUpSeconds(groupUsers, startVusers);
            long duration = getDurationSeconds(group.getScheduler().getActions());
            results.add(buildResult(group.getName(), groupUsers, rampUpTime, duration, testStartTime));
        }

        return results;
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
    private long getDurationSeconds(List<Action> actions) {
        Duration duration = getFirstAction(actions, Action::getDuration, "duration");
        if (duration.getType() != SchedulerDurationType.RUN_FOR) return 0L;
        TimeInterval timeInterval = duration.getTimeInterval();
        return timeInterval.toDuration().getSeconds();
    }

    /**
     * Optimized ramp calculation assuming guarantees
     * No null checks for ramp since it's guaranteed for GRADUALLY type
     */
    private long calculateRampUpSeconds(int totalUsers, StartVusers startVusers) {
        if (startVusers.getType() == SIMULTANEOUSLY) return 0L;

        Ramp ramp = startVusers.getRamp();        // GRADUALLY - ramp is guaranteed non-null with vusers > 0
        int batchSize = ramp.getVusers();
        if (totalUsers <= batchSize) return 0L;   // All users start in first batch if totalUsers <= batchSize

        // Calculate number of intervals (batches - 1). Using integer math for efficiency
        int intervals = (totalUsers - 1) / batchSize;
        TimeInterval timeInterval = ramp.getTimeInterval();

        java.time.Duration intervalDuration = timeInterval.toDuration();
        return intervalDuration.multipliedBy(intervals).getSeconds();
    }

    private SteadyStateResult buildResult(String name, int totalUsers, long rampUpTime,
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
    }

    private int calculateTotalUsers() {
        WorkloadVusersDistributionMode mode = content.getWorkloadType().getVusersDistributionMode();
        if (mode == WorkloadVusersDistributionMode.BY_PERCENTAGE) return content.getTotalVusers();
        else {  // BY_NUMBER
            int sum = 0;
            for (Group group : content.getGroups()) sum += group.getVusers();
            return sum;
        }
    }
}