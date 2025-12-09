package com.lre.services.lre.calculation;

import com.lre.model.enums.SchedulerStartGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SteadyStateResult {
    // Basic info
    private String groupName;
    private String workloadType;
    private Integer totalUsers;

    // StartGroup configuration
    private SchedulerStartGroupType startGroupType;
    private String dependencyGroupName;
    private Long delaySeconds;

    // Timing metrics
    private Long rampUpTimeSeconds;
    private Long steadyStateDurationSeconds;
    private Long rampDownTimeSeconds;
    private Long groupTotalTimeSeconds;  // rampUp + steady + rampDown

    // Offsets
    private Long groupStartOffsetSeconds;  // When group starts relative to test

    // Relative to group start
    private Long relativeToGroupStartSeconds;
    private Long relativeToGroupEndSeconds;

    // Relative to test start
    private Long relativeToTestStartSeconds;
    private Long relativeToTestEndSeconds;
    private Long relativeGroupEndSeconds;

    // Absolute epoch times
    private Long absoluteTestStartEpoch;
    private Long absoluteGroupStartEpoch;
    private Long absoluteSteadyStateStartEpoch;
    private Long absoluteSteadyStateEndEpoch;
    private Long absoluteGroupEndEpoch;
}