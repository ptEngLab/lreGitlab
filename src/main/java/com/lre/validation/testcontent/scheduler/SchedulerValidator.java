package com.lre.validation.testcontent.scheduler;

import com.lre.model.test.testcontent.TestContent;
import com.lre.model.test.testcontent.scheduler.Scheduler;
import com.lre.model.test.testcontent.scheduler.action.Action;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.lre.actions.utils.ConfigConstants.*;

@Slf4j
public class SchedulerValidator {

    private final TestContent content;
    private final String workloadTypeStr;

    private static final Set<String> SUPPORTED_ACTIONS = Set.of(
            "startgroup", "initialize", "startvusers", "duration", "stopvusers"
    );

    public SchedulerValidator(TestContent content) {
        this.content = content;
        this.workloadTypeStr = content.getWorkloadType().getWorkloadTypeAsStr();
    }

    /**
     * Validates and parses the scheduler YAML provided as a list of maps.
     * Falls back to default scheduler if input is null or empty.
     */
    public Scheduler validateScheduler(List<Map<String, String>> schedulerItems, int vusersCount) {
        if (schedulerItems == null || schedulerItems.isEmpty()) {
            log.info("No scheduler items provided, using default scheduler for workload type: {}", workloadTypeStr);
            return getDefaultSchedulerForWorkload(vusersCount);
        }

        List<Action> parsedActions = new ArrayList<>();

        for (Map<String, String> item : schedulerItems) {
            for (Map.Entry<String, String> entry : item.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT).trim();
                if (!SUPPORTED_ACTIONS.contains(key)) {
                    log.error("Unsupported scheduler key: '{}'. Supported keys: {}", key, SUPPORTED_ACTIONS);
                    return null;
                }
                String value = Optional.ofNullable(entry.getValue()).orElse("").trim();
                Action action = parseSchedulerItemFromKeyValue(key, value, vusersCount);
                if (action != null) parsedActions.add(action);
            }
        }

        validateAction(parsedActions, vusersCount);

        return new Scheduler(parsedActions);
    }

    /**
     * Returns a default scheduler based on workload type.
     */
    private Scheduler getDefaultSchedulerForWorkload(int vusersCount) {
        return switch (workloadTypeStr) {
            case basicByTest -> Scheduler.getDefaultSchedulerForBasicByTest();
            case realWorldByTest -> Scheduler.getDefaultSchedulerForRBTest(vusersCount);
            case basicByGroup -> Scheduler.getDefaultSchedulerForBasicByGroup();
            case realWorldByGroup -> Scheduler.getDefaultSchedulerForRBGrp(vusersCount);
            default -> {
                log.info("No default scheduler for workload type: {}", workloadTypeStr);
                yield new Scheduler();
            }
        };
    }


    /**
     * Parses a single scheduler key-value pair into an Action object.
     */
    private Action parseSchedulerItemFromKeyValue(String key, String value, int vusersCount) {
        try {
            return switch (key) {
                case "startgroup" -> Action.builder()
                        .startGroup(new StartGroupValidator(content).validateGroup(value))
                        .build();
                case "initialize" -> Action.builder()
                        .initialize(new InitializeValidator().validateInitialize(value))
                        .build();
                case "startvusers" -> Action.builder()
                        .startVusers(new StartVusersValidator(workloadTypeStr, vusersCount).validateStart(value))
                        .build();
                case "duration" -> Action.builder()
                        .duration(new DurationValidator(workloadTypeStr).validateDuration(value))
                        .build();
                case "stopvusers" -> Action.builder()
                        .stopVusers(new StopVusersValidator(workloadTypeStr, vusersCount).validateStop(value))
                        .build();
                default -> {
                    log.error("Unsupported scheduler key: {}", key);
                    yield null;
                }
            };
        } catch (IllegalArgumentException e) {
            log.error("Validation error for scheduler item '{}:{}': {}", key, value, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error parsing scheduler item '{}:{}': {}", key, value, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Validates actions, sorts them in execution order.
     */
    private void validateAction(List<Action> actions, int vusersCount) {
        if (actions.isEmpty()) return;

        new StartGroupValidator(content).validateStartGroupActions(actions);
        new InitializeValidator().validateInitializeActions(actions);
        new StartVusersValidator(workloadTypeStr, vusersCount).validateStartVusersActions(actions);
        new DurationValidator(workloadTypeStr).validateDurationActions(actions);
        new StopVusersValidator(workloadTypeStr, vusersCount).validateStopVusersActions(actions);

        actions.sort(Comparator.comparingInt(this::getActionOrder));
    }

    /**
     * Determines execution order of actions.
     */
    private int getActionOrder(Action action) {
        boolean isRealWorld = workloadTypeStr.startsWith("real-world");

        if (isRealWorld) {
            if (action.getStartGroup() != null) return 0;
            if (action.getInitialize() != null) return 1;
        } else { // basic or other workloads
            if (action.getInitialize() != null) return 0;
        }

        // All other actions (startVusers, duration, stopVusers) come after first two
        return 2; // same value ensures relative order is preserved by stable sort
    }

}
