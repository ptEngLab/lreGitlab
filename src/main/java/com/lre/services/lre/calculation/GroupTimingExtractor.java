package com.lre.services.lre.calculation;

import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.enums.SchedulerStartGroupType;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.Ramp;
import com.lre.model.test.testcontent.scheduler.action.common.VusersAction;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.experimental.UtilityClass;

import static com.lre.model.enums.SchedulerVusersType.SIMULTANEOUSLY;

@UtilityClass
public class GroupTimingExtractor {

    /**
     * Extracts timing information from a group's scheduler configuration.
     *
     * @param group the group to extract timing info from
     * @return GroupTimingInfo containing all calculated timing metrics
     */
    public static GroupTimingInfo extract(Group group) {
        Scheduler scheduler = group.getScheduler();
        int totalUsers = group.getVusers();

        StartGroup startGroup = ActionUtils.getFirstAction(scheduler.getActions(), Action::getStartGroup, group.getName());
        StartVusers startVusers = ActionUtils.getFirstAction(scheduler.getActions(), Action::getStartVusers, group.getName());
        Duration duration = ActionUtils.getFirstAction(scheduler.getActions(), Action::getDuration, group.getName());
        StopVusers stopVusers = ActionUtils.getFirstAction(scheduler.getActions(), Action::getStopVusers, group.getName());

        long rampUpSeconds = calculateRampSeconds(totalUsers, startVusers);
        long durationSeconds = getDurationSeconds(duration);
        long rampDownSeconds = calculateRampSeconds(totalUsers, stopVusers);

        Long delaySeconds = null;
        if (startGroup.getType() == SchedulerStartGroupType.WITH_DELAY) {
            // TimeInterval is guaranteed for WITH_DELAY type by LRE
            delaySeconds = startGroup.getTimeInterval().toDuration().getSeconds();
        }

        return new GroupTimingInfo(
                group.getName(), totalUsers, startGroup,
                rampUpSeconds, durationSeconds, rampDownSeconds, delaySeconds
        );
    }

    /**
     * Calculates duration in seconds from a Duration action.
     *
     * @param duration the Duration action
     * @return duration in seconds, or 0 for non-RUN_FOR durations
     */
    public static long getDurationSeconds(Duration duration) {
        if (duration.getType() != SchedulerDurationType.RUN_FOR) return 0L;
        // TimeInterval is guaranteed for RUN_FOR type by LRE
        return duration.getTimeInterval().toDuration().getSeconds();
    }

    /**
     * Calculates ramp time in seconds for a VusersAction (StartVusers or StopVusers).
     *
     * @param totalUsers total number of users
     * @param action     the VusersAction (StartVusers or StopVusers)
     * @return ramp time in seconds
     */
    public static long calculateRampSeconds(int totalUsers, VusersAction action) {
        if (action.getType() == SIMULTANEOUSLY) return 0L;

        // Ramp is guaranteed for GRADUALLY type by LRE
        Ramp ramp = action.getRamp();
        int batchSize = ramp.getVusers();
        if (totalUsers <= batchSize) return 0L;

        int intervals = (totalUsers - 1) / batchSize;
        // TimeInterval is guaranteed for Ramp by LRE
        return ramp.getTimeInterval().toDuration().multipliedBy(intervals).getSeconds();
    }
}