package com.lre.common.utils;

import com.lre.model.enums.SchedulerDurationType;
import com.lre.model.enums.SchedulerInitializeType;
import com.lre.model.enums.SchedulerStartGroupType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.common.VusersAction;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.initialize.Initialize;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class SchedulerActionFactory {

    public static Action startGroupDefault() {
        StartGroup startGroup = new StartGroup();
        startGroup.setType(SchedulerStartGroupType.IMMEDIATELY);
        return Action.builder().startGroup(startGroup).build();
    }

    public static Action initializeDefault() {
        Initialize init = new Initialize();
        init.setType(SchedulerInitializeType.JUST_BEFORE_VUSER_RUNS);
        return Action.builder().initialize(init).build();
    }

    public static Action startVusersDefault() {
        return buildVusers(new StartVusers(), null);
    }

    public static Action startVusers(int totalVusers) {
        return buildVusers(new StartVusers(), totalVusers);
    }

    public static Action stopVusersDefault() {
        return buildVusers(new StopVusers(), null);
    }

    public static Action stopVusers(int totalVusers) {
        return buildVusers(new StopVusers(), totalVusers);
    }

    public static Action durationUntilCompletion() {
        return buildDuration(SchedulerDurationType.UNTIL_COMPLETION, null);
    }

    public static Action durationRunFor(TimeInterval interval) {
        if (interval == null) throw new IllegalArgumentException("TimeInterval cannot be null");
        return buildDuration(SchedulerDurationType.RUN_FOR, interval);
    }

    private static <T extends VusersAction> Action buildVusers(T vusersAction, Integer totalVusers) {
        if (vusersAction == null) throw new IllegalArgumentException("VusersAction cannot be null");
        if (totalVusers != null) vusersAction.setVusers(totalVusers);
        Action.ActionBuilder builder = Action.builder();
        vusersAction.applyTo(builder);
        return builder.build();
    }

    private static Action buildDuration(SchedulerDurationType type, TimeInterval interval) {
        Duration duration = new Duration();
        duration.setType(type);
        if (type == SchedulerDurationType.RUN_FOR) duration.setTimeInterval(interval);
        return Action.builder().duration(duration).build();
    }
}
