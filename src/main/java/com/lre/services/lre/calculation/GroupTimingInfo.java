package com.lre.services.lre.calculation;

import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupTimingInfo {
    private String groupName;
    private int totalUsers;
    private StartGroup startGroup;
    private long rampUpSeconds;
    private long durationSeconds;
    private long rampDownSeconds;
    private Long delaySeconds;
}