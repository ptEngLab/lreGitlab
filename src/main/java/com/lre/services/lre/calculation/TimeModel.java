package com.lre.services.lre.calculation;

import com.lre.model.enums.SchedulerStartGroupType;

/**
 * Encapsulates all timing calculations for a group's steady state.
 * Provides both relative and absolute time calculations.
 */
public record TimeModel(
        long testStartTimeEpoch,
        long groupStartOffsetSeconds,
        long rampUpSeconds,
        long durationSeconds,
        long rampDownSeconds
) {

    public long groupTotalTimeSeconds() {
        return rampUpSeconds + durationSeconds + rampDownSeconds;
    }

    /**
     * Time when steady-state starts (relative to group start).
     */
    public long relativeToGroupStartSeconds() {
        return rampUpSeconds;
    }

    /**
     * Time when steady-state ends (relative to group start).
     */
    public long relativeToGroupEndSeconds() {
        return rampUpSeconds + durationSeconds;
    }

    /**
     * Time when group fully ends (relative to group start).
     */
    public long relativeGroupEndSeconds() {
        return groupTotalTimeSeconds();
    }

    /**
     * Time when steady-state begins relative to test start.
     */
    public long relativeToTestStartSeconds() {
        return groupStartOffsetSeconds + rampUpSeconds;
    }

    /**
     * Time when steady-state ends relative to test start.
     */
    public long relativeToTestEndSeconds() {
        return groupStartOffsetSeconds + rampUpSeconds + durationSeconds;
    }

    /**
     * Time when group ends relative to test start.
     */
    public long relativeGroupEndFromTestStartSeconds() {
        return groupStartOffsetSeconds + groupTotalTimeSeconds();
    }

    public long absoluteGroupStartEpoch() {
        return testStartTimeEpoch + groupStartOffsetSeconds;
    }

    public long absoluteSteadyStateStartEpoch() {
        return absoluteGroupStartEpoch() + rampUpSeconds;
    }

    public long absoluteSteadyStateEndEpoch() {
        return durationSeconds > 0
                ? absoluteGroupStartEpoch() + rampUpSeconds + durationSeconds
                : 0;
    }

    public long absoluteGroupEndEpoch() {
        return absoluteGroupStartEpoch() + groupTotalTimeSeconds();
    }

    public static TimeModel forGroup(long testStartTimeEpoch, long groupOffset, GroupTimingInfo timing) {
        return new TimeModel(testStartTimeEpoch, groupOffset, timing.getRampUpSeconds(), timing.getDurationSeconds(), timing.getRampDownSeconds());
    }

    public static TimeModel forTest(long testStartTimeEpoch, long rampUpSeconds, long durationSeconds, long rampDownSeconds) {
        return new TimeModel(testStartTimeEpoch, 0L, rampUpSeconds, durationSeconds, rampDownSeconds);
    }

    public SteadyStateResult toSteadyStateResult(String groupName, int totalUsers, String workloadType, SchedulerStartGroupType startGroupType,
                                                 String dependencyGroup, Long delaySeconds) {
        return SteadyStateResult.builder()
                .groupName(groupName)
                .totalUsers(totalUsers)
                .workloadType(workloadType)

                .startGroupType(startGroupType)
                .dependencyGroupName(dependencyGroup)
                .delaySeconds(delaySeconds)

                .rampUpTimeSeconds(rampUpSeconds)
                .steadyStateDurationSeconds(durationSeconds)
                .rampDownTimeSeconds(rampDownSeconds)
                .groupTotalTimeSeconds(groupTotalTimeSeconds())

                .groupStartOffsetSeconds(groupStartOffsetSeconds)

                // Relative
                .relativeToGroupStartSeconds(relativeToGroupStartSeconds())
                .relativeToGroupEndSeconds(relativeToGroupEndSeconds())
                .relativeToTestStartSeconds(relativeToTestStartSeconds())
                .relativeToTestEndSeconds(relativeToTestEndSeconds())
                .relativeGroupEndSeconds(relativeGroupEndFromTestStartSeconds())

                // Absolute timestamps
                .absoluteTestStartEpoch(testStartTimeEpoch)
                .absoluteGroupStartEpoch(absoluteGroupStartEpoch())
                .absoluteSteadyStateStartEpoch(absoluteSteadyStateStartEpoch())
                .absoluteSteadyStateEndEpoch(absoluteSteadyStateEndEpoch())
                .absoluteGroupEndEpoch(absoluteGroupEndEpoch())

                .build();
    }
}
