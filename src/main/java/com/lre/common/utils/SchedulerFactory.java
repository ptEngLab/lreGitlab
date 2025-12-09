package com.lre.common.utils;

import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class SchedulerFactory {


    /**
     * Default scheduler for "Basic By Test"
     */
    public static Scheduler getDefaultSchedulerForBasicByTest() {
        return Scheduler.builder()
                .actions(List.of(
                        SchedulerActionFactory.initializeDefault(),
                        SchedulerActionFactory.startVusersDefault(),
                        SchedulerActionFactory.durationUntilCompletion()
                ))
                .build();
    }

    /**
     * Default scheduler for "Real-World By Test" with total Vusers
     */
    public static Scheduler getDefaultSchedulerForRBTest(int totalVusers) {
        return Scheduler.builder()
                .actions(List.of(
                        SchedulerActionFactory.initializeDefault(),
                        SchedulerActionFactory.startVusers(totalVusers),
                        SchedulerActionFactory.durationRunFor(TimeInterval.builder().minutes(5).build()),
                        SchedulerActionFactory.stopVusersDefault()
                ))
                .build();
    }


    /**
     * Default scheduler for "Basic By Group"
     */
    public static Scheduler getDefaultSchedulerForBasicByGroup() {
        return Scheduler.builder()
                .actions(List.of(
                        SchedulerActionFactory.startGroupDefault(),
                        SchedulerActionFactory.initializeDefault(),
                        SchedulerActionFactory.startVusersDefault(),
                        SchedulerActionFactory.durationUntilCompletion()
                ))
                .build();
    }

    /**
     * Default scheduler for "Real-World By Group" with total Vusers
     */
    public static Scheduler getDefaultSchedulerForRBGrp(int totalVusers) {
        return Scheduler.builder()
                .actions(List.of(
                        SchedulerActionFactory.startGroupDefault(),
                        SchedulerActionFactory.initializeDefault(),
                        SchedulerActionFactory.startVusers(totalVusers),
                        SchedulerActionFactory.durationRunFor(TimeInterval.builder().minutes(5).build()),
                        SchedulerActionFactory.stopVusers(totalVusers)
                ))
                .build();
    }
}
