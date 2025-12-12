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

    // Identification
    private String groupName;
    private String workloadType;
    private Integer totalUsers;

    // StartGroup metadata
    private SchedulerStartGroupType startGroupType;
    private String dependencyGroupName;
    private Long delaySeconds;

    // Timing (from TimeModel)
    private Long rampUpTimeSeconds;
    private Long steadyStateDurationSeconds;
    private Long rampDownTimeSeconds;
    private Long groupTotalTimeSeconds;

    // Offsets
    private Long groupStartOffsetSeconds;

    // Relative times
    private Long relativeToGroupStartSeconds;
    private Long relativeToGroupEndSeconds;
    private Long relativeToTestStartSeconds;
    private Long relativeToTestEndSeconds;
    private Long relativeGroupEndSeconds;

    // Absolute times
    private Long absoluteTestStartEpoch;
    private Long absoluteGroupStartEpoch;
    private Long absoluteSteadyStateStartEpoch;
    private Long absoluteSteadyStateEndEpoch;
    private Long absoluteGroupEndEpoch;
}
