package com.lre.services.lre.calculation;

import com.lre.model.enums.WorkloadSubType;
import com.lre.model.enums.WorkloadTypeEnum;
import com.lre.model.enums.WorkloadVusersDistributionMode;
import com.lre.model.test.Test;
import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<SteadyStateResult> calculateSteadyState(long testStartTimeEpoch) {
        WorkloadTypeEnum type = content.getWorkloadType().getType();
        WorkloadSubType subType = content.getWorkloadType().getSubType();

        if (type != WorkloadTypeEnum.BASIC) {
            log.warn("Unsupported workload type: {}. Returning empty steady state results.", type);
            return List.of();
        }

        return subType == WorkloadSubType.BY_TEST
                ? List.of(calculateBasicByTest(testStartTimeEpoch))
                : calculateBasicByGroup(testStartTimeEpoch);
    }

    private SteadyStateResult calculateBasicByTest(long testStartTimeEpoch) {
        int totalUsers = calculateTotalUsers();
        Scheduler scheduler = content.getScheduler();

        StartVusers startVusers = ActionUtils.getFirstAction(scheduler.getActions(), Action::getStartVusers, "test startVusers");
        Duration duration = ActionUtils.getFirstAction(scheduler.getActions(), Action::getDuration, "test duration");
        StopVusers stopVusers = ActionUtils.getFirstAction(scheduler.getActions(), Action::getStopVusers, "test stopVusers");

        long rampUpSeconds = GroupTimingExtractor.calculateRampSeconds(totalUsers, startVusers);
        long durationSeconds = GroupTimingExtractor.getDurationSeconds(duration);
        long rampDownSeconds = GroupTimingExtractor.calculateRampSeconds(totalUsers, stopVusers);

        SteadyStateInput input = new SteadyStateInput(
                lreTest.getName(),
                totalUsers,
                testStartTimeEpoch,
                0L, // group start offset
                rampUpSeconds,
                durationSeconds,
                rampDownSeconds,
                null, // startGroupType not relevant for by-test
                null, // dependencyGroup
                null  // delaySeconds
        );

        return buildResult(input);
    }

    private List<SteadyStateResult> calculateBasicByGroup(long testStartTimeEpoch) {
        List<Group> groups = content.getGroups();
        List<SteadyStateResult> results = new ArrayList<>(groups.size());

        Map<String, GroupTimingInfo> timingInfo = extractAllGroupTimingInfo(groups);
        Map<String, Long> startOffsets = GroupOffsetCalculator.calculateOffsets(timingInfo);

        for (Group group : groups) {
            String groupName = group.getName();
            GroupTimingInfo info = timingInfo.get(groupName);

            SteadyStateInput input = new SteadyStateInput(
                    info.getGroupName(),
                    info.getTotalUsers(),
                    testStartTimeEpoch,
                    startOffsets.get(groupName),
                    info.getRampUpSeconds(),
                    info.getDurationSeconds(),
                    info.getRampDownSeconds(),
                    info.getStartGroup().getType(),
                    info.getStartGroup().getName(),
                    info.getDelaySeconds()
            );

            results.add(buildResult(input));
        }

        return results;
    }

    private Map<String, GroupTimingInfo> extractAllGroupTimingInfo(List<Group> groups) {
        Map<String, GroupTimingInfo> timingInfo = new HashMap<>();
        for (Group group : groups) {
            timingInfo.put(group.getName(), GroupTimingExtractor.extract(group));
        }
        return timingInfo;
    }

    private SteadyStateResult buildResult(SteadyStateInput input) {
        TimeModel timeModel = new TimeModel(
                input.getTestStartTimeEpoch(),
                input.getGroupStartOffsetSeconds(),
                input.getRampUpSeconds(),
                input.getDurationSeconds(),
                input.getRampDownSeconds()
        );

        return timeModel.toSteadyStateResult(
                input.getName(),
                input.getTotalUsers(),
                workloadTypeStr,
                input.getStartGroupType(),
                input.getDependencyGroup(),
                input.getDelaySeconds()
        );
    }

    private int calculateTotalUsers() {
        WorkloadVusersDistributionMode mode = content.getWorkloadType().getVusersDistributionMode();
        if (mode == WorkloadVusersDistributionMode.BY_PERCENTAGE) return content.getTotalVusers();
        return content.getGroups().stream().mapToInt(Group::getVusers).sum();
    }
}
