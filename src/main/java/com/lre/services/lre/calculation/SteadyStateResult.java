package com.lre.services.lre.calculation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SteadyStateResult {
    private int totalUsers;
    private long rampUpTimeSeconds;
    private long steadyStateDurationSeconds;
    private String workloadType;
    private long steadyStateStartEpoch;
    private long steadyStateEndEpoch;
    private String groupName;
}
