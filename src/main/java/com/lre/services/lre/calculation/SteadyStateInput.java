package com.lre.services.lre.calculation;

import com.lre.model.enums.SchedulerStartGroupType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SteadyStateInput {
    private final String name;
    private final int totalUsers;
    private final long testStartTimeEpoch;
    private final long groupStartOffsetSeconds;
    private final long rampUpSeconds;
    private final long durationSeconds;
    private final long rampDownSeconds;
    private final SchedulerStartGroupType startGroupType;
    private final String dependencyGroup;
    private final Long delaySeconds;


}
