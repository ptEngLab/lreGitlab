package com.lre.actions.lre.testcontentvalidator.scheduler;

import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.duration.Duration;
import com.lre.model.test.testcontent.scheduler.action.initialize.Initialize;
import com.lre.model.test.testcontent.scheduler.action.startgroup.StartGroup;
import com.lre.model.test.testcontent.scheduler.action.startvusers.StartVusers;
import com.lre.model.test.testcontent.scheduler.action.stopvusers.StopVusers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.lre.actions.helpers.ConfigConstants.*;

@Slf4j
public class SchedulerValidator {

    private final TestContent content;
    private final String workloadTypeStr;

    private static final String START_GROUP = "startgroup:";
    private static final String INITIALIZE = "initialize:";
    private static final String START_VUSERS = "startvusers:";
    private static final String DURATION = "duration:";
    private static final String STOP_VUSERS = "stopvusers:";


    public SchedulerValidator(TestContent content) {
        this.content = content;
        this.workloadTypeStr = content.getWorkloadType().getWorkloadTypeAsStr();
    }

    public Scheduler validateScheduler(List<String> schedulerItems, int vusersCount) {
        Scheduler scheduler;
        switch (workloadTypeStr) {

            case basicByTest -> {
                return schedulerItems.isEmpty()
                        ? Scheduler.getDefaultSchedulerForBasicByTest()
                        : parseSchedulerItems(schedulerItems, vusersCount);
            }

            case realWorldByTest -> {
                if (schedulerItems.isEmpty()) scheduler = Scheduler.getDefaultSchedulerForRBTest(vusersCount);
                else scheduler = parseSchedulerItems(schedulerItems, vusersCount);
                return scheduler;
            }

            case basicByGroup -> {
                content.setScheduler(null); // Scheduler is per group. so root level scheduler will be null
                if (schedulerItems.isEmpty()) scheduler = Scheduler.getDefaultSchedulerForBasicByGroup();
                else scheduler = parseSchedulerItems(schedulerItems, vusersCount);
                return scheduler;
            }

            case realWorldByGroup -> {
                content.setScheduler(null); // Scheduler is per group, clear root scheduler
                if (schedulerItems.isEmpty()) scheduler = Scheduler.getDefaultSchedulerForRBGrp(vusersCount);
                else scheduler = parseSchedulerItems(schedulerItems, vusersCount);
                return scheduler;
            }

            default -> {
                log.info("No scheduler generated for workload type: {}", workloadTypeStr);
                return new Scheduler();
            }
        }
    }

    private Scheduler parseSchedulerItems(List<String> schedulerItems, int vusersCount) {
        List<Action> parsedActions = new ArrayList<>();

        for (String item : schedulerItems) {
            String input = StringUtils.deleteWhitespace(item).toLowerCase(Locale.ROOT);
            Action action = parseSchedulerItem(input, vusersCount);
            if (action != null) parsedActions.add(action);
        }

        validateAction(parsedActions, vusersCount);

        return new Scheduler(parsedActions);
    }

    private Action parseSchedulerItem(String input, int vusersCount) {
        try {

            if (input.startsWith(START_GROUP)) {
                String groupInput = input.substring(START_GROUP.length()).trim();
                StartGroup startGroupAction = new StartGroupValidator(content).validateGroup(groupInput);
                return Action.builder().startGroup(startGroupAction).build();

            } else if (input.startsWith(INITIALIZE)) {
                String initInput = input.substring(INITIALIZE.length()).trim();
                Initialize init = new InitializeValidator().validateInitialize(initInput);
                return Action.builder().initialize(init).build();

            } else if (input.startsWith(START_VUSERS)) {
                String startInput = input.substring(START_VUSERS.length()).trim();
                StartVusers startVusersAction = new StartVusersValidator(workloadTypeStr, vusersCount).validateStart(startInput);
                return Action.builder().startVusers(startVusersAction).build();

            } else if (input.startsWith(DURATION)) {
                String durationInput = input.substring(DURATION.length()).trim();
                Duration durationAction = new DurationValidator(workloadTypeStr).validateDuration(durationInput);
                return Action.builder().duration(durationAction).build();

            } else if (input.startsWith(STOP_VUSERS)) {
                String stopInput = input.substring(STOP_VUSERS.length()).trim();
                StopVusers stopVusersAction = new StopVusersValidator(workloadTypeStr, vusersCount).validateStop(stopInput);
                return Action.builder().stopVusers(stopVusersAction).build();

            } else {
                log.error("Unsupported scheduler item: {}", input);
                return null;
            }

        } catch (Exception e) {
            log.error("Error parsing scheduler item '{}': {}", input, e.getMessage());
            return null;
        }
    }


    private void validateAction(List<Action> actions, int vusersCount) {
        if (actions.isEmpty()) return;

        new StartGroupValidator(content).validateStartGroupActions(actions);
        new InitializeValidator().validateInitializeActions(actions);
        new StartVusersValidator(workloadTypeStr, vusersCount).validateStartVusersActions(actions);
        new DurationValidator(workloadTypeStr).validateDurationActions(actions);
        new StopVusersValidator(workloadTypeStr, vusersCount).validateStopVusersActions(actions);
        actions.sort(Comparator.comparingInt(this::getActionOrder));

    }

    private int getActionOrder(Action a) {
        if (a.getStartGroup() != null) return 0;
        if (a.getInitialize() != null) return 1;
        if (a.getStartVusers() != null) return 2;
        if (a.getDuration() != null) return 3;
        if (a.getStopVusers() != null) return 4;
        return 5;
    }


}
