package com.lre.validation.testcontent.scheduler;

import com.lre.model.enums.SchedulerInitializeType;
import com.lre.model.test.testcontent.scheduler.action.Action;
import com.lre.model.test.testcontent.scheduler.action.common.TimeInterval;
import com.lre.model.test.testcontent.scheduler.action.initialize.Initialize;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

import static com.lre.actions.utils.ConfigConstants.*;
import static com.lre.model.test.testcontent.scheduler.action.common.TimeInterval.parseTimeInterval;

@Slf4j
public record InitializeValidator() {

    public Initialize validateInitialize(String input) {
        Initialize init = new Initialize(); // default: JUST_BEFORE_VUSER_RUNS
        if (StringUtils.isBlank(input)) return init;

        input = StringUtils.trimToEmpty(input).toLowerCase();
        Matcher sim = SIMULTANEOUSLY_PATTERN.matcher(input);
        Matcher grad = GRADUALLY_PATTERN.matcher(input);
        Matcher just = JUST_BEFORE_PATTERN.matcher(input);

        if (sim.matches()) {
            init.setType(SchedulerInitializeType.SIMULTANEOUSLY);
            setWaitAfterInit(init, sim);
        } else if (grad.matches()) {
            init.setType(SchedulerInitializeType.GRADUALLY);
            init.setVusersFromString(grad.group("users"));
            init.setTimeInterval(parseTimeInterval(grad.group("interval")));
            setWaitAfterInit(init, grad);
        } else if (just.matches()) {
            log.debug("[Scheduler] Parsed JUST_BEFORE initialize (default).");
        } else {
            log.debug("[Scheduler] Unrecognized initialize '{}', using default: {}", input, init.getType());
        }

        return init;
    }

    private void setWaitAfterInit(Initialize init, Matcher matcher) {
        String waitGroup = matcher.group("wait");
        if (StringUtils.isNotBlank(waitGroup))
            init.setWaitAfterInit(parseTimeInterval(waitGroup));
        else
            init.setWaitAfterInit(new TimeInterval());
    }

    public void validateInitializeActions(List<Action> actions) {
        List<Action> initializes = actions.stream().filter(a -> a.getInitialize() != null).toList();

        if (initializes.isEmpty()) {
            log.warn("[Scheduler] No Initialize action found. Adding default Initialize.");
            int insertIndex = Math.min(1, actions.size());
            actions.add(insertIndex, Action.initializeDefault());
        } else if (initializes.size() > 1) {
            log.warn("[Scheduler] Multiple Initialize actions found. Keeping first.");
            Action first = initializes.get(0);
            actions.removeIf(a -> a.getInitialize() != null && a != first);
        }
    }
}
